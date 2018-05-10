package com.jgardo.skypebot.util

import com.jgardo.skypebot.config.Text
import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import javax.inject.Inject

class TextTranslator @Inject constructor(vertx:Vertx)  {
    lateinit var config :JsonObject

    init {
        ConfigRetriever.create(vertx).getConfig({
            ar -> config = ar.result()
        })
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