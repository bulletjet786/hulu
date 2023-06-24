package `fun`.deckz.hulu.api.version

import kotlinx.serialization.Serializable

class ModuleName {
    companion object {
        const val STARTER: String = "starter"
        const val LET: String = "let"
        const val PAD: String = "pad"
    }
}

@Serializable
class HuluModuleVersion(
    val starter: ModuleVersion,
    val let: ModuleVersion,
    val pad: ModuleVersion
)

typealias VersionLatestRequest = HuluModuleVersion

@Serializable
class HuluModuleLocation(
    val starter: ModuleLocation,
    val let: ModuleLocation,
    val pad: ModuleLocation
)

typealias VersionLatestResponse = HuluModuleLocation

@Serializable
class ModuleVersion(
    val name: String,
    val version: String
)

@Serializable
class ModuleLocation(
    val name: String,
    val version: String,
    val downloadUrl: String,
    val md5: String
)