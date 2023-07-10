package `fun`.deckz.hulu.pad

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import `fun`.deckz.hulu.api.common.HuluResponse
import `fun`.deckz.hulu.api.extension.*
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.z4kn4fein.semver.Version
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.system.exitProcess

private val myVersion = Version(
    major = 0, minor = 0, patch = 1
)

private val logger = KotlinLogging.logger("Pad")

@Composable
@Preview
fun App() {

    MaterialTheme {
        MainView()
    }
}

private val client = HttpClient(CIO) {
    defaultRequest {
        host = "localhost"
        port = 8282
        header(HttpHeaders.ContentType, ContentType.Application.Json)
    }
    install(ContentNegotiation) {
        json(Json {
            encodeDefaults = true
            prettyPrint = true
            ignoreUnknownKeys = true
        })
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.HEADERS
    }
}

private suspend fun getExtensions(): ListExtensionResponse {
    // get extensions
    val extensionList = client.post {
        url("/extension/list")
        setBody(
            ListExtensionRequest()
        )
    }.body<HuluResponse<ListExtensionResponse>>()
    if ((extensionList.status.code) != 0) {
        throw RuntimeException("status code not zero")
    }
    return extensionList.data!!
}

private suspend fun installExtension(name: String) {
    // install extension
    val extensionList = client.post {
        url("/extension/install")
        setBody(
            InstallExtensionRequest(name = name)
        )
    }.body<HuluResponse<InstallExtensionResponse>>()
    if ((extensionList.status.code) != 0) {
        throw RuntimeException("status code not zero")
    }
}

fun main(argv: Array<String>) = runBlocking {
    application {
        if (argv.isNotEmpty()) {
            if (argv[0] == "version") {
                print(myVersion)
                exitProcess(0)
            }
        }
        Window(onCloseRequest = ::exitApplication) {
            App()
        }
    }
}

@Composable
@Preview
fun MainView() {
    // state
    val selectedTabIndex = remember { mutableStateOf(0) }

    // properties
    val tables = arrayOf("Extension", "Feedback") // TODO

    Column {
        TabRow(
            selectedTabIndex = selectedTabIndex.value
        ) {
            tables.forEachIndexed { index, title ->
                Tab(
                    text = {
                        Text(title)
                    },
                    selected = selectedTabIndex.value == index,
                    onClick = {
                        selectedTabIndex.value = index
                    }
                )
            }
        }

        when (selectedTabIndex.value) {
            0 -> ExtensionView()
            1 -> FeedbackView()
        }
    }
}

@Composable
fun FeedbackView() {

    val feedbackType = arrayOf("bug", "feature")
    val selectedType = remember { mutableStateOf("feature") }

    Column {
        Row {
            Text("Feedback Type: ")

            feedbackType.forEach {
                RadioButton(
                    selected = it == selectedType.value,
                    onClick = {
                        selectedType.value = it
                    },
                    modifier = Modifier.padding(2.dp),
                    enabled = true
                )
                Text(text = it, textAlign = TextAlign.Center)
            }
        }
    }

}

@Composable
fun ExtensionView() {
    val extensions = remember { mutableStateListOf<Extension>() }
    CoroutineScope(Dispatchers.IO).launch {
        logger.info { "start getExtension ..." }
        val extensionResponse = getExtensions()
        logger.info { "extension response: ${Json.encodeToString(extensionResponse)}"}
        extensions.addAll(extensionResponse.extensions)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
    ) {
        items(items = extensions) {
            ExtensionCard(it)
        }
    }
}

@Composable
fun ExtensionCard(extension: Extension) {
    val painter = painterResource(extension.icon)
    Column {
        Image(
            painter = painter,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            contentDescription = extension.description
        )
        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = {
                when {
                    !extension.installed -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            installExtension(extension.name)
                        }
                    }
                    extension.installed -> {

                    }
                }
                println("install fcitx5")
            }
        ) {
            when {
                !extension.installed -> Text("Install")
                extension.installed -> Text("Run")
            }
        }
    }
}
