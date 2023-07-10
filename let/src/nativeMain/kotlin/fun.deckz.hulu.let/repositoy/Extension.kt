package `fun`.deckz.hulu.let.repositoy

import `fun`.deckz.hulu.let.domain.model.ExtensionDeploy
import `fun`.deckz.hulu.let.domain.model.ExtensionInfo
import `fun`.deckz.hulu.let.domain.model.Sandbox

object ExtensionRepository {

    val extensions = mapOf(
        "fcitx" to ExtensionInfo(
            name = "fcitx", description = "Fcitx", icon = "./fcitx/fcitx.png",
            deploy = ExtensionDeploy(
                install = Sandbox(command = "/bin/sh", argv = listOf("-c", "./deploy/install.sh")),
                uninstall = Sandbox(command = "/bin/sh", argv = listOf("-c", "./deploy/uninstall.sh")),
                check = Sandbox(command = "/bin/sh", argv = listOf("-c", "./deploy/check.sh"))
            ),
            functions = mapOf(
                "start" to Sandbox(command = "/bin/sh", listOf("-c", "./functions/start.sh"))
            )
        )
    )
}