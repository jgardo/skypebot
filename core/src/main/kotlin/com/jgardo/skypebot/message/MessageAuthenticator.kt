package com.jgardo.skypebot.message

import com.google.common.cache.CacheBuilder
import com.jgardo.skypebot.config.BasicConfig
import com.jgardo.skypebot.config.ConfigService
import com.jgardo.skypebot.vertx.VertxUtils
import io.vertx.core.Future
import io.vertx.core.MultiMap
import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.codec.BodyCodec
import javax.inject.Inject

class MessageAuthenticator @Inject constructor(private val vertx:Vertx, private val configService: ConfigService){

    private val accessTokenCache = CacheBuilder.newBuilder()
            .build<Class<String>, String>()!!

    private val logger = LoggerFactory.getLogger(this::class.java)

    private var client : WebClient = WebClient.create(vertx)
    private val authenticationForm = MultiMap.caseInsensitiveMultiMap()

    fun getAccessToken() : Future<String> {
        val clientId = configService.getString(BasicConfig.AUTHENTICATION_CLIENT_ID)?: VertxUtils.missingConfig(BasicConfig.AUTHENTICATION_CLIENT_ID)
        val clientSecret = configService.getString(BasicConfig.AUTHENTICATION_CLIENT_SECRET)?: VertxUtils.missingConfig(BasicConfig.AUTHENTICATION_CLIENT_SECRET)
        authenticationForm.set("grant_type", "client_credentials")
                .set("client_id", clientId) //MICROSOFT-APP-ID
                .set("client_secret", clientSecret) //MICROSOFT-APP-PASSWORD
                .set("scope", "https://api.botframework.com/.default")
        if (logger.isDebugEnabled) {
            val sb = StringBuilder()
                    .appendln("Authentication properties:")
                    .appendln("clientId: ${VertxUtils.shortenSensitiveString(clientId)}")
                    .appendln("clientSecret: ${VertxUtils.shortenSensitiveString(clientSecret)}")
            logger.debug(sb.toString())
        }

        val fromCache : String? = accessTokenCache.getIfPresent(String::class.java)
        if (fromCache != null) {
            if (logger.isDebugEnabled) {
                logger.debug("Authenticated by value from cache")
            }
            return Future.succeededFuture(fromCache)
        }

        if (logger.isDebugEnabled) {
            logger.debug("Authenticating")
        }

        val future = Future.future<String>()
        client.postAbs("https://login.microsoftonline.com/botframework.com/oauth2/v2.0/token").`as`(BodyCodec.jsonObject())
                .putHeader("Content-Type","application/x-www-form-urlencoded")
                .sendForm(authenticationForm, {ar ->

                    if (logger.isDebugEnabled) {
                        logger.debug("Authentication finishes!")
                    }

                    val result = ar.result()
                    val json = result.body()

                    if (result.statusCode() != 200) {
                        future.fail("Wrong status code in response. Full response: $result")
                        return@sendForm
                    }
                    val tokenType : String? = json.getString("token_type")!!
                    if (tokenType!! != "Bearer") {
                        future.fail("Wrong token_type in response. Full response: $result")
                        return@sendForm
                    }
                    val expiresIn = json.getLong("expires_in")!!
                    if (expiresIn < 0) {
                        future.fail("Invalid expires_in in response. Full response: $result")
                        return@sendForm
                    }

                    val accessToken = json.getString("access_token")!!

                    accessTokenCache.put(String::class.java, accessToken)
                    vertx.setTimer((expiresIn - 5) * 1000, {accessTokenCache.invalidateAll()})

                    if (logger.isDebugEnabled) {
                        logger.debug("Authenticated!")
                    }

                    future.complete(accessToken)
                })
        return future
    }
}