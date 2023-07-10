package `fun`.deckz.hulu.let.extensions

import `fun`.deckz.hulu.let.repositoy.ExtensionRepository
import `fun`.deckz.hulu.process.Process
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

fun configureExtensionDaemon() {
    val logger = KotlinLogging.logger("ExtensionDaemon")

    val WORK_DIR: String = "/opt/fun.deckz/hulu"

    CoroutineScope(Dispatchers.Default).launch {
        while (true) {
            ExtensionRepository.extensions.forEach {
                val checkProc = Process.start(
                    it.value.deploy.check.command,
                    it.value.deploy.check.argv,
                    workdir = "$WORK_DIR/data/extensions/${it.key}",
                    uid = 1000,
                    pipe = true
                )
                checkProc.waitExited()
                when (checkProc.readStdout()) {
                    "Installed" -> it.value.installed = true
                    "Uninstalled" -> it.value.installed = false
                }
            }
            delay(10.seconds)
        }
    }

}