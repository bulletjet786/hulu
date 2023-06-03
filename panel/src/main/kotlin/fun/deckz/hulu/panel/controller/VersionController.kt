package `fun`.deckz.hulu.panel.controller

import `fun`.deckz.hulu.api.common.HuluResponse
import `fun`.deckz.hulu.api.version.VersionLatestRequest
import `fun`.deckz.hulu.api.version.VersionLatestResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController("/version")
class VersionController {

    @PostMapping("/latest")
    fun latest(@RequestBody request: VersionLatestRequest): HuluResponse<VersionLatestResponse> {
        return HuluResponse.of(
            VersionLatestResponse(
                starterVersion = "0.0.1",
                starterDownloadUrl = "https://hulu.deckz.fun/starter.zip",
                starterMd5 = "abc",
                huluVersion = "0.0.1",
                huluDownloadUrl = "https://hulu.deckz.fun/hulu.zip",
                huluMd5 = "def",
                )
        )
    }
}