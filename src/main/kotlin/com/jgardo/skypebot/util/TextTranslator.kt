package com.jgardo.skypebot.util

import com.jgardo.skypebot.config.Text
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject

class TextTranslator : Handler<AsyncResult<JsonObject>> {

    lateinit var config :JsonObject

    override fun handle(ar: AsyncResult<JsonObject>) {
        config = ar.result()
    }

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