package `fun`.deckz.hulu.process

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.*
import platform.posix.*

class Sandbox(
    val ID: String,
    val processID: Int?,
    val stdinFd: Int,
    val stdoutFd: Int,
    starterPid: Int,
    letPid: Int?,
    padPid: Int?
)

/*
class Process {
    private val stdin: String
    private val stdout: String
}
*/

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

    fun startCmd(cmd: String, argv: Array<String>?): Int? {
        val ret = fork()
        if (ret < 0) {
            logger.error { "start cmd process failed: command=$cmd" }
            return null
        }
        logger.info { "start cmd process, fork success: command=$cmd args=${argv?.joinToString()}" }
        return if (ret == 0) {   // for child process
            var execRet = 0
            if (argv == null) {
                execRet = execvp(cmd, null)
            } else {
                memScoped {
                    val cArgs = allocArray<CPointerVar<ByteVar>>(argv.size + 2)
                    cArgs[0] = cmd.cstr.ptr
                    argv.forEachIndexed { i, it ->
                        cArgs[i + 1] = it.cstr.ptr
                        logger.info { "argv on $i is ${cArgs[i + 1]?.toKString()}" }
                    }
                    logger.info { "argv on ${argv.size + 1} is ${cArgs[argv.size + 1]}" }
                    execRet = execvp(cmd, cArgs)
                }
            }
            if (execRet != 0) {
                logger.error { "exec ret value is $execRet, errno is $errno" }
                return execRet
            }
            0
        } else {    // for parent process
            logger.info { "start cmd process success: command=$cmd, childPid=$ret" }
            ret
        }
    }
}