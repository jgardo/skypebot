package com.jgardo.skypebot.util

import com.jgardo.skypebot.config.Text
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject

class TextTranslator  {
    val configHandler : Handler<AsyncResult<JsonObject>> = Handler {
        ar -> config = ar.result()
    }

    lateinit var config :JsonObject

    fun translate(text: Text) : String{
        return translate(text, HashMap())
    }

    fun translate(text: Text, params: Map<String, Any>) : String{
        val template = if (config.getString(text.code) is String) config.getString(text.code)
                else text.defaultValue

        var result = template
        params.entries.forEach {
            (key, value) -> result = result.replace(":$key","$value")
        }

        return result
    }

}