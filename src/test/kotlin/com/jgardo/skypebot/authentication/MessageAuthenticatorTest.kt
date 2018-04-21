package com.jgardo.skypebot.authentication

import com.jgardo.skypebot.message.MessageAuthenticator
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(VertxUnitRunner::class)
class MessageAuthenticatorTest {

    private var vertx: Vertx? = null

    @Before
    fun setUp(context: TestContext) {
        vertx = Vertx.vertx()
    }

    @After
    fun tearDown(context: TestContext) {
        vertx!!.close(context.asyncAssertSuccess())
    }

    @Test
    fun testMyApplication(context: TestContext) {
        val vertx = vertx!!
        val async = context.async()
        vertx.deployVerticle(MessageAuthenticator::class.java.name,
                { ar ->
                    if (ar.succeeded()) {
                        vertx.eventBus().send("getAccessToken", "", {authenticationResult : AsyncResult<Message<String>> ->
                            val accessToken = vertx.sharedData().getLocalMap<String,String>("authentication")["accessToken"]
                            context.assertNotNull(accessToken)
                            println("Access token : $accessToken")
                            context.assertTrue(authenticationResult.succeeded())
                            async.complete()
                        })
                    } else {
                        context.fail(ar.cause())
                        async.complete()
                    }
                })

    }

}