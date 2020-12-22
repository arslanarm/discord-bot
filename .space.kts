
job("Build and Publish") {
    docker {
        build {}
        push("mana-renewal.registry.jetbrains.space/p/discord-bot/discord-bot/myimage") {
            tag = "v1"
        }
    }
}
