package `fun`.deckz.hulu.api.extension

import kotlinx.serialization.Serializable


@Serializable
data class ListExtensionRequest(val type: String? = null)

@Serializable
data class ListExtensionResponse(
    val extensions: List<Extension>
)

@Serializable
data class Extension(
    val name: String,
    val description: String,
    val icon: String,
    val installed: Boolean = false
)

@Serializable
data class InstallExtensionRequest(val name: String)

@Serializable
class InstallExtensionResponse