package com.jgardo.skypebot.authentication

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
class AuthenticationVerticleTest {

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
        vertx.deployVerticle(AuthenticationVerticle::class.java.name,
                { ar ->
                    if (ar.succeeded()) {
                        vertx.eventBus().send("authenticate", "", {authenticationResult : AsyncResult<Message<String>> ->
                            context.assertNotNull(vertx.sharedData().getLocalMap<String,String>("authentication")["accessToken"])
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