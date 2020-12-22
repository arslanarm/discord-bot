
job("Build and Publish") {
    val tag = "v1"
    docker {
        build {}
        push("mana-renewal.registry.jetbrains.space/p/discord-bot/discord-bot/myimage") {
            this.tag = tag
        }
    }
    container("mana-renewal.registry.jetbrains.space/p/discord-bot/discord-bot/upload:latest") {
        env["AUTHORIZATION"] = Secrets("authorization")
        env["SERVER_IP"] = Secrets("server_ip")
        env["BOT_TOKEN"] = Secrets("token")
        env["REPOSITORY"] = "mana-renewal.registry.jetbrains.space/p/discord-bot/discord-bot/myimage"
        env["TAG"] = tag
        env["NAME"] = "discord-bot"

        shellScript {
            content = "python3 /app/upload.py"
        }
    }
}
