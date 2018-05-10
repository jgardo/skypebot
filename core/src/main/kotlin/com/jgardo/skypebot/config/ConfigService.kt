package com.jgardo.skypebot.config

import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.kotlin.core.json.get
import java.util.stream.Collectors
import javax.inject.Inject

class ConfigService @Inject constructor(vertx: Vertx, configProviders : java.util.Set<ConfigProvider>) {

    private lateinit var configMap : Map<Config, Any>

    init {
        ConfigRetriever.create(vertx).getConfig({
            ar -> configMap =
                configProviders.stream()
                        .flatMap {
                            it.configs.stream()
                                    .filter({ it != null })
                                    .filter({ ar.result().containsKey(it.configName) })
                        }
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