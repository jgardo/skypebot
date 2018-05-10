package com.jgardo.skypebot.message

import com.jgardo.skypebot.message.model.Message
import io.vertx.config.ConfigRetriever
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.eventbus.Message as VertxMessage
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(VertxUnitRunner::class)
class MessageVerticleTest {

    private lateinit var vertx: Vertx

    @Before
    fun setUp(context: TestContext) {
        vertx = Vertx.vertx()
    }

    @After
    fun tearDown(context: TestContext) {
        vertx.close(context.asyncAssertSuccess())
    }

    @Test
    fun testMyApplication(context: TestContext) {
        val async = context.async()
        ConfigRetriever.create(vertx)
            .getConfig {config->
                val accessToken = config.result()
                        .getJsonObject("authentication")
                        .getString("hardcodedAccessToken")

                vertx.sharedData().getLocalMap<String,String>("authentication")["accessToken"] = accessToken

                vertx.deployVerticle(MessageVerticle::class.java.name,
                    { ar ->
                        if (ar.succeeded()) {
                            val message = Message("test",null, "test")

                            vertx.eventBus().send(MessageBusEvent.SEND.eventName, Json.encode(message), { sendResult : AsyncResult<VertxMessage<Message>> ->
                                context.assertTrue(sendResult.succeeded())
                                async.complete()
                            })
                        } else {
                            context.fail(ar.cause())
                            async.complete()
                        }
                    })
            }

    }

}