package `fun`.deckz.hulu.panel.controller

import `fun`.deckz.hulu.api.common.HuluResponse
import `fun`.deckz.hulu.api.version.*
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
                starter = ModuleLocation(
                    name = ModuleName.STARTER,
                    version = "0.0.2",
                    downloadUrl = "http://150.158.135.143:7777/hulu/starter/0.0.2/starter.tar.gz",
                    md5 = ""
                ),
                let = ModuleLocation(
                    name = ModuleName.LET,
                    version = "0.0.2",
                    downloadUrl = "http://150.158.135.143:7777/hulu/let/0.0.2/let.tar.gz",
                    md5 = ""
                ),
                pad = ModuleLocation(
                    name = ModuleName.PAD,
                    version = "0.0.1",
                    downloadUrl = "http://150.158.135.143:7777/hulu/pad/0.0.1/pad.tar.gz",
                    md5 = ""
                )
            )
        )
    }
}