
job("Build and Publish") {
    docker {
        build {
            context = "docker"
            file = "./Dockerfile"
        }
        push("mana-renewal.registry.jetbrains.space/p/discord-bot/discord-bot/myimage:latest") {
            tag = "v1"
        }
    }
}
