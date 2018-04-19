package com.jgardo.skypebot.notification

import com.google.inject.Guice
import com.jgardo.skypebot.server.BaseRoute
import com.jgardo.skypebot.message.model.Message
import com.jgardo.skypebot.notification.authorization.model.OpenIdMetadataDocument
import com.jgardo.skypebot.notification.authorization.model.SigningKeysWrapper
import com.jgardo.skypebot.notification.model.Activity
import com.jgardo.skypebot.util.TextTranslator
import com.jgardo.skypebot.util.VertxUtils
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.eventbus.MessageProducer
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.codec.BodyCodec

class NotificationRoute(private val appId : String,
                        private val messageSender : MessageProducer<Message>,
                        private val textTranslator: TextTranslator) : BaseRoute() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun configure(router: Router, vertx: Vertx) {
        val injector = Guice.createInjector(NotificationModule(appId, messageSender, textTranslator))

        val webClient : WebClient = WebClient.create(vertx)

        router.post("/notification/*")
                .configureRestRoutingWithBody()
                .handler { ctx ->
                    val authorization = ctx.request().getHeader("Authorization")?: "not available"
                    logger.info("Authorization header: $authorization")

                    val firstRequest: Future<OpenIdMetadataDocument> = prepareFirstFuture(webClient)
                    val secondRequest : Future<AllResults> = firstRequest.compose {
                        return@compose prepareSecondFuture(webClient, it)
                    }

                    val authorized : Future<Boolean> = secondRequest.compose {
                        val firstResponse = it.openIdMetadataDocument
                        val secondResponse = it.signingKeysWrapper

                        return@compose Future.succeededFuture(true)
                    }.recover {
                        return@recover Future.succeededFuture(false)
                    }

                    authorized.compose {
                        val body = ctx.bodyAsJson

                        if (logger.isDebugEnabled) {
                            logger.debug(body.encodePrettily())
                        } else {
                            logger.info(body.encodePrettily())
                        }

                        val activity = body.mapTo(Activity::class.java)
                        val notificationController = injector.getInstance(NotificationController::class.java)

                        notificationController.notify(activity)

                        ctx.response().end("<h1>Notified!</h1>")
                        return@compose Future.succeededFuture<Void>()
                    }
                }

    }

    private fun prepareFirstFuture(webClient: WebClient): Future<OpenIdMetadataDocument> {
        val future: Future<OpenIdMetadataDocument> = Future.future<OpenIdMetadataDocument>()
        webClient.getAbs("https://login.botframework.com/v1/.well-known/openidconfiguration")
                .`as`(BodyCodec.jsonObject())
                .send({ ar ->
                    VertxUtils.wrap(ar, { resp ->
                        future.complete(resp.body().mapTo(OpenIdMetadataDocument::class.java))
                    }, { ex -> future.fail(ex) })
                })
        return future
    }

    private fun prepareSecondFuture(webClient: WebClient, openIdMetadataDocument: OpenIdMetadataDocument): Future<AllResults> {
        val future: Future<AllResults> = Future.future<AllResults>()
        webClient.getAbs(openIdMetadataDocument.jwksUri)
                .`as`(BodyCodec.jsonObject())
                .send({ ar ->
                    VertxUtils.wrap(ar, { resp ->
                        val wrapper = resp.body().mapTo(SigningKeysWrapper::class.java)
                        future.complete(AllResults(openIdMetadataDocument,wrapper))
                    }, { ex -> future.fail(ex) })
                })
        return future
    }

    private data class AllResults(val openIdMetadataDocument: OpenIdMetadataDocument, val signingKeysWrapper: SigningKeysWrapper)
}