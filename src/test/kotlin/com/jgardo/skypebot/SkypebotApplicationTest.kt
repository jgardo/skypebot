package com.jgardo.skypebot

import io.vertx.core.Vertx
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(VertxUnitRunner::class)
class SkypebotApplicationTest {

    private var vertx: Vertx? = null

    @Before
    fun setUp(context: TestContext) {
        vertx = Vertx.vertx()
        vertx!!.deployVerticle(SkypebotApplication::class.java.name,
                context.asyncAssertSuccess())
    }

    @After
    fun tearDown(context: TestContext) {
        vertx!!.close(context.asyncAssertSuccess())
    }

    @Test
    fun testMyApplication(context: TestContext) {
        val async = context.async()

        vertx!!.createHttpClient().getNow(8080, "localhost", "/",
                { response ->
                    response.handler({ body ->
                        context.assertTrue(body.toString().contains("Hello"))
                        async.complete()
                    })
                })
    }

}