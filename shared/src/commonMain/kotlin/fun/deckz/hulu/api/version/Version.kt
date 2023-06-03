package `fun`.deckz.hulu.api.version

import kotlinx.serialization.Serializable

@Serializable
class VersionLatestRequest(
    val myStarterVersion: String,
    val myHuluVersion: String,
)

@Serializable
class VersionLatestResponse(
    val starterVersion: String,
    val starterDownloadUrl: String,
    val starterMd5: String,
    val huluVersion: String,
    val huluDownloadUrl: String,
    val huluMd5: String
)
