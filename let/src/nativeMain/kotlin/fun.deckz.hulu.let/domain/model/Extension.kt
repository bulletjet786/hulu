package `fun`.deckz.hulu.let.domain.model

import `fun`.deckz.hulu.api.extension.Extension

data class ExtensionInfo(
    val name: String,
    val description: String,
    val icon: String,
    var installed: Boolean = false,
    val deploy: ExtensionDeploy,
    val functions: Map<String, Sandbox>
) {
    fun toExtension(): Extension {
        return Extension(name, description, icon, installed)
    }
}

data class ExtensionDeploy(
    val install: Sandbox,
    val uninstall: Sandbox,
    val check: Sandbox
)

data class Sandbox(
    val command: String,
    val argv: List<String>? = null
)