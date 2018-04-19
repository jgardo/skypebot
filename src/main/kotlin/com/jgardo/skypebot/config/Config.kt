package com.jgardo.skypebot.config

enum class Config(val configName : String, val sensitive : Boolean) {
    SERVER_PORT("server.port",true),
    APP_ID("appid",true),
    BASE_URL("baseurl",true),
    AUTHENTICATION_CLIENT_ID("authentication.clientId",true),
    AUTHENTICATION_CLIENT_SECRET("authentication.clientSecret",true)
}