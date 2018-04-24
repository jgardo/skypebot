package com.jgardo.skypebot.config

import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.kotlin.core.json.get
import java.util.*
import java.util.stream.Collectors
import javax.inject.Inject

class ConfigService @Inject constructor(vertx: Vertx) {

    private lateinit var configMap : Map<Config, Any>

    init {
        ConfigRetriever.create(vertx).getConfig({
            ar -> configMap =
                Arrays.stream(Config.values())
                        .filter({it != null})
                        .filter({ ar.result().containsKey(it.configName)})
                        .collect(Collectors.toMap({it}, { ar.result()[it.configName] }))
        })
    }

    fun getString(config:Config) : String? {
        return configMap[config] as String?
    }

    fun getLong(config:Config) : Long? {
        return configMap[config] as Long?
    }
}