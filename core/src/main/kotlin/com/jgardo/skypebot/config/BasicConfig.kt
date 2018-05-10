package com.jgardo.skypebot.config

enum class BasicConfig(override val configName : String, override val sensitive : Boolean) : Config {
    SERVER_PORT("server.port",true),
    APP_ID("appid",true),
    BASE_URL("baseurl",true),
    AUTHENTICATION_CLIENT_ID("authentication.clientId",true),
    AUTHENTICATION_CLIENT_SECRET("authentication.clientSecret",true)
}

class BaseConfigProvider : ConfigProvider {
    override val configs: Set<Config>
        get() = BasicConfig.values().toSet()
}