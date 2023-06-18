package `fun`.deckz.hulu.api.sandbox

import kotlinx.serialization.Serializable

@Serializable
data class CreateSandboxRequest(
    val command: String,
    val argv: Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CreateSandboxRequest

        if (command != other.command) return false
        return argv.contentEquals(other.argv)
    }

    override fun hashCode(): Int {
        var result = command.hashCode()
        result = 31 * result + argv.contentHashCode()
        return result
    }
}

@Serializable
data class CreateSandboxResponse(
    val sandboxID: String
)

@Serializable
data class GetSandboxRequest(
    val sandboxID: String
)

@Serializable
data class GetSandboxResponse(
    val status: String
)