package com.jgardo.skypebot.vertx

import com.jgardo.skypebot.config.Config
import io.vertx.core.AsyncResult
import io.vertx.core.logging.LoggerFactory

object VertxUtils {
    private val logger = LoggerFactory.getLogger(this::class.java)
    fun <T> wrap(ar:AsyncResult<T>, f : (T) -> Unit, onError: (Throwable) -> Unit) {
        if (ar.succeeded()) {
            try {
                f(ar.result())
            } catch (e : RuntimeException) {
                logger.error("Exception during invoking function.", e)
                onError(e)
            }
        } else {
            logger.error("Async result with error.", ar.cause())
            onError(ar.cause())
        }
    }

    fun <T> wrap(ar:AsyncResult<T>, f : (T) -> Unit) {
        wrap(ar, f, {})
    }

    fun shortenSensitiveString(source: String?) : String {
        return if (source != null) {
            source.take(10) + "..."
        } else {
            "null"
        }
    }

    fun missingConfig(config : Config) : String {
        throw IllegalStateException("Config property is missing: \"${config.configName}\"")
    }
}