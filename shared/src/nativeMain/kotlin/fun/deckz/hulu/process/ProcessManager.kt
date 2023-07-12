package `fun`.deckz.hulu.process

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.*
import platform.linux.PR_SET_PDEATHSIG
import platform.linux.prctl
import platform.posix.*
import kotlin.native.concurrent.AtomicInt

class Process private constructor(
    private val pid: Int,
    private val pipe: Boolean,
    private val workdir: String? = null,
    private val uid: Int? = null,
    private val stdoutPipe: CArrayPointer<IntVar>?,
    private val stderrPipe: CArrayPointer<IntVar>?,
    private var exitCode: Int? = null
) {

    private val alive: AtomicInt = AtomicInt(1) // TODO: should use AtomicBoolean, but don't have

    companion object {

        private fun exitCodeFromProcess(status: Int): Int {
            return (status shr 8) and 0xFF
        }

        init {
            signal(SIGCHLD, staticCFunction<Int, Unit> {
                childProcessHandler(it)
            })
        }

        private val logger = KotlinLogging.logger("Process")
        private val children: MutableMap<Int, Process> = mutableMapOf()

        private fun childProcessHandler(sig: Int) {
            memScoped {
                val status = alloc<IntVar>()
                while (true) {
                    val pid = waitpid(-1, status.ptr, WNOHANG)
                    logger.info { "wait pid=${pid}, status=${status.value}, exit status=${exitCodeFromProcess(status.value)}" }
                    if (pid <= 0) {
                        break;
                    }
                    if (pid in children) {
                        children[pid]!!.alive(false, exitCodeFromProcess(status.value))
                    }
                }
            }
        }

        fun start(
            cmd: String,
            argv: List<String>?,
            workdir: String? = null,
            uid: Int? = null,
            pipe: Boolean = false
        ): Process {
            var stdoutPipe: CArrayPointer<IntVar>? = null
            var stderrPipe: CArrayPointer<IntVar>? = null
            if (pipe) {
                stdoutPipe = makePipe()
                stderrPipe = makePipe()
            }
            val ret = fork()
            if (ret < 0) {
                throw RuntimeException("fork failed: error is $errno")
            }
            if (ret == 0) {   // for child process, child process don't return
                if (pipe) {
                    configChildPipe(stdoutPipe!!, stderrPipe!!)
                }
                configChildProcess(cmd, argv, workdir, uid)
            }
            // for parent process
            logger.info { "start cmd process success: [command=$cmd] [argv=${argv?.joinToString()}] [workdir=${workdir}] [uid=${uid}] [childPid=$ret]" }
            if (pipe) {
                configParentPipe(stdoutPipe!!, stderrPipe!!)
            }
            val childProcess = Process(ret, pipe, workdir, uid, stdoutPipe, stderrPipe)
            children[childProcess.pid] = childProcess
            return childProcess
        }

        fun systemCmd(cmd: String): Int {
            return system(cmd)
        }

        private fun makePipe(): CArrayPointer<IntVar> {
            val pipe = nativeHeap.allocArray<IntVar>(2)
            val ret = pipe(pipe)
            if (ret < 0) {
                throw RuntimeException("make pipe failed: error is $errno")
            }
            return pipe
        }

        private fun configChildProcess(
            cmd: String,
            argv: List<String>?,
            workdir: String?,
            uid: Int?
        ) {
            prctl(PR_SET_PDEATHSIG, SIGKILL);
            if (workdir != null) {
                chdir(workdir)
            }
            if (uid != null) {
                setuid(uid.toUInt())
            }
            memScoped {
                val cArgv = allocArray<CPointerVar<ByteVar>>((argv?.size ?: 0) + 2)
                cArgv[0] = cmd.cstr.ptr
                argv?.forEachIndexed { i, it ->
                    cArgv[i + 1] = it.cstr.ptr
                }
                val execRet = execvp(cmd, cArgv) // don't return when success
                if (execRet != 0) {
                    throw RuntimeException("execvp failed: error is $errno")
                }
            }
        }

        private fun configChildPipe(
            stdoutPipe: CArrayPointer<IntVar>,
            stderrPipe: CArrayPointer<IntVar>,
        ) {
            close(stdoutPipe[0])
            close(stderrPipe[0])
            val stdoutDupRet = dup2(stdoutPipe[1], STDOUT_FILENO)
            if (stdoutDupRet == -1) {
                throw RuntimeException("dup2 stdout failed: errno is $errno")
            }
            val stderrDupRet = dup2(stderrPipe[1], STDERR_FILENO)
            if (stderrDupRet == -1) {
                throw RuntimeException("dup2 stderr failed: errno is $errno")
            }
        }

        private fun configParentPipe(
            stdoutPipe: CArrayPointer<IntVar>,
            stderrPipe: CArrayPointer<IntVar>,
        ) {
            close(stdoutPipe[1])
            close(stderrPipe[1])
        }
    }

    fun waitExited() {
        memScoped {
            val status = alloc<IntVar>()
            if (alive()) {
                waitpid(pid, status.ptr, 0)
                logger.info { "wait pid=${pid}, status=${status.value}, exit code=${exitCodeFromProcess(status.value)}" }
                alive(false, exitCodeFromProcess(status.value))
            }
        }
    }

    fun alive(): Boolean {
        return alive.value == 1
    }

    fun exitCode(): Int {
        return this.exitCode!!
    }

    private fun alive(value: Boolean, code: Int) {
        logger.info { "make alive=${value} exitCode={$code}" }
        if (value) this.alive.value = 1 else this.alive.value = 0
        this.exitCode = code
    }

    fun kill() {
        kill(pid, SIGILL)
    }

    fun readStdout(): String {
        if (!pipe) {
            throw RuntimeException("pipe disabled")
        }
        return readAll(stdoutPipe!![0])
    }

    fun readStderr(): String {
        if (!pipe) {
            throw RuntimeException("pipe disabled")
        }
        return readAll(stderrPipe!![0])
    }

    private fun readAll(fd: Int): String {
        memScoped {
            val size = 1024L
            val buf: COpaquePointer = allocArray<ByteVar>(size)
            val totalBuf = mutableListOf<Byte>()
            while (true) {
                val count = read(fd, buf, size.toULong())
                if (count == 0L) {
                    break;
                }
                totalBuf.addAll(buf.readBytes(count.toInt()).asList())
            }
            val content = totalBuf.toByteArray().decodeToString()
            logger.info { "pid=$pid read stdout: content=$content" }
            return content
        }
    }

    fun clear() {
        if (pipe) {
            nativeHeap.free(stdoutPipe!!)
            nativeHeap.free(stderrPipe!!)
        }
    }
}

// zombie process:
// 1) waitpid
// 2) sigchld
// for() { if (waitpid(NOHANG) == 0) break;}

object ProcessManager {

    private val logger = KotlinLogging.logger {}

    fun getCurrentPid(): Int {
        return getpid()
    }

    fun getPidFromPidFile(filePath: String): Int? {
        val pidFile = fopen(filePath, "r")
        return if (pidFile == null) {
            null
        } else {
            val pid = memScoped {
                val pidBuffer: CArrayPointer<ByteVar> = nativeHeap.allocArray<ByteVar>(32)
                fread(pidBuffer, 32, 1, pidFile)
                pidBuffer.toKString()
            }
            pid.toInt()
        }
    }

    fun writePidToPidFile(pid: Int, filePath: String) {
        val pidFile = fopen(filePath, "w")
        if (pidFile == null) {
            throw RuntimeException("can't open filePath=$filePath, because of $errno")
        } else {
            fwrite(pid.toString().cstr, pid.toString().length.toULong(), 1, pidFile)
            logger.info { "write pid=$pid to $filePath" }
            fclose(pidFile)
        }
    }

    fun removePidFile(filePath: String) {
        unlink(filePath)
    }
}