package `fun`.deckz.hulu.pad

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.z4kn4fein.semver.Version
import kotlin.system.exitProcess

private val myVersion = Version(
    major = 0, minor = 0, patch = 1
)

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello World") }

    MaterialTheme {
        Card(
            modifier = Modifier.width(100.dp).height(100.dp)
        ) {
            Button(onClick = {
                text = "Hello Hulu"
            }) {
                Text(text)
            }
        }

    }
}

fun main(argv: Array<String>) = application {
    if (argv.isNotEmpty()) {
        if (argv[0] == "version") {
            println(myVersion)
            exitProcess(0)
        }
    }
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
