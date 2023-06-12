package `fun`.deckz.hulu.panel

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.ApplicationPidFileWriter
import org.springframework.boot.runApplication

@SpringBootApplication
class PanelApplication

fun main(args: Array<String>) {
    runApplication<PanelApplication>(*args) {
        addListeners(ApplicationPidFileWriter())
    }
}
