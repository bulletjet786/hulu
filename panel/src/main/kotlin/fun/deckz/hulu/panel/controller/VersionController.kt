package `fun`.deckz.hulu.panel.controller

import `fun`.deckz.hulu.api.common.HuluResponse
import `fun`.deckz.hulu.api.version.VersionLatestRequest
import `fun`.deckz.hulu.api.version.VersionLatestResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/version")
class VersionController {

    @PostMapping("/latest")
    fun latest(@RequestBody request: VersionLatestRequest): HuluResponse<VersionLatestResponse> {
        return HuluResponse.of(
            VersionLatestResponse(
                starterVersion = "0.0.1",
                starterDownloadUrl = "http://150.158.135.143:7777/starter.zip",
                starterMd5 = "c56d75dfdcaf33f959fee7f039923f0c",
                huluVersion = "0.0.1",
                huluDownloadUrl = "http://150.158.135.143:7777/hulu/0.0.2",
                huluMd5 = "c56d75dfdcaf33f959fee7f039923f0c",
                )
        )
    }
}