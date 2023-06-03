package `fun`.deckz.hulu.pad

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Install Clash") }

    MaterialTheme {
        Card(
            modifier = Modifier.width(100.dp).height(100.dp)
        ) {
            Button(onClick = {
                text = "Install Finished"
            }) {
                Text(text)
            }
        }

    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
