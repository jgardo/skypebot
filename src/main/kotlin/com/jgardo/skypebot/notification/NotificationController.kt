package com.jgardo.skypebot.notification

import io.vertx.core.json.JsonObject

class NotificationController {

    fun notify(jsonObject: JsonObject) {
        println(jsonObject.toString())
    }
}