package com.jgardo.skypebot.util

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import io.vertx.core.eventbus.impl.codecs.JsonObjectMessageCodec
import io.vertx.core.json.JsonObject

class ObjectMessageCodec<T>(private val clazz:Class<T>) : MessageCodec<T,T> {
    private val jsonObjectMessageCodec : JsonObjectMessageCodec = JsonObjectMessageCodec()

    override fun decodeFromWire(p0: Int, p1: Buffer?): T {
        return jsonObjectMessageCodec.decodeFromWire(p0, p1).mapTo(clazz)
    }

    override fun encodeToWire(p0: Buffer?, p1: T) {
        return jsonObjectMessageCodec.encodeToWire(p0, JsonObject.mapFrom(p1))
    }

    override fun transform(p0: T): T {
        return p0
    }

    override fun name(): String {
        return "customSerializer${clazz.canonicalName}"
    }

    override fun systemCodecID(): Byte {
        return -1
    }
}