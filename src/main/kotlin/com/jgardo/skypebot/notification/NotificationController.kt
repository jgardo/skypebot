package com.jgardo.skypebot.notification

import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory

class NotificationController {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun notify(jsonObject: JsonObject) {
        if (logger.isDebugEnabled) {
            logger.debug(jsonObject.encodePrettily())
        }
    }
}