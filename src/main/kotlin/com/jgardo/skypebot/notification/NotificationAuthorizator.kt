package com.jgardo.skypebot.notification

import com.google.common.cache.CacheBuilder
import com.jgardo.skypebot.notification.authorization.model.OpenIdMetadataDocument
import com.jgardo.skypebot.notification.authorization.model.SigningKeysWrapper
import com.jgardo.skypebot.vertx.VertxUtils
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.util.Base64
import com.nimbusds.jose.util.X509CertChainUtils
import com.nimbusds.jwt.SignedJWT
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.codec.BodyCodec
import net.minidev.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NotificationAuthorizator @Inject constructor(vertx: Vertx) {
    private val webClient: WebClient = WebClient.create(vertx)

    private val openIdMetadataCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5L, TimeUnit.DAYS)
            .build<Class<OpenIdMetadataDocument>, OpenIdMetadataDocument>()!!

    private val signingKeysWrapperCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5L, TimeUnit.DAYS)
            .build<Class<SigningKeysWrapper>, SigningKeysWrapper>()!!

    fun validate(authorizationHeader : String) : Future<Boolean> {
        val firstRequest: Future<OpenIdMetadataDocument> = prepareFirstFuture(webClient)
        val secondRequest : Future<AllResults> = firstRequest.compose {
            return@compose prepareSecondFuture(webClient, it)
        }

        return secondRequest.compose {
            val firstResponse = it.openIdMetadataDocument
            val secondResponse = it.signingKeysWrapper

            val signedJWT: SignedJWT? = extractSignedJWT(authorizationHeader)

            if (isAlgorithmInvalid(firstResponse, signedJWT)) return@compose Future.succeededFuture(false)
            if (isCertInvalid(secondResponse, signedJWT)) return@compose Future.succeededFuture(false)

            val payload = signedJWT!!.payload.toJSONObject()

            if (isIssuerInvalid(payload)) return@compose Future.succeededFuture(false)
            if (isDateInvalid(payload)) return@compose Future.succeededFuture(false)

            return@compose Future.succeededFuture(true)
        }.recover {
            return@recover Future.succeededFuture(false)
        }
    }

    private fun extractSignedJWT(authorizationHeader: String): SignedJWT? {
        val compactJws = authorizationHeader.substring("Bearer ".length)

        var signedJWT: SignedJWT? = null

        try {
            signedJWT = SignedJWT.parse(compactJws)!!
        } catch (e: Throwable) {

        }
        return signedJWT
    }

    private fun isAlgorithmInvalid(firstResponse: OpenIdMetadataDocument, signedJWT: SignedJWT?): Boolean {
        if (!firstResponse.idTokenSigningAlgValuesSupported.contains(signedJWT!!.header.algorithm.name)) {
            return true
        }
        return false
    }

    private fun isCertInvalid(secondResponse: SigningKeysWrapper, signedJWT: SignedJWT?): Boolean {
        val signedKey = secondResponse.keys.find { it.kid == signedJWT!!.header.keyID }!!

        val cert = X509CertChainUtils.parse(signedKey.x5c.map { Base64(it) })
        val rsaJWK = RSAKey.parse(cert[0])
        val verifier: JWSVerifier = RSASSAVerifier(rsaJWK)
        if (!signedJWT!!.verify(verifier)) {
            return true
        }
        return false
    }

    private fun isIssuerInvalid(payload: JSONObject): Boolean {
        if (payload.getAsString("iss") != "https://api.botframework.com") {
            return true
        }
        return false
    }

    private fun isDateInvalid(payload: JSONObject): Boolean {
        val now = Date()
        val nbf = Date(payload.getAsString("nbf").toLong() * 1000)
        val exp = Date(payload.getAsString("exp").toLong() * 1000)
        if (now.before(nbf) || now.after(exp)) {
            return true
        }
        return false
    }

    private fun prepareFirstFuture(webClient: WebClient): Future<OpenIdMetadataDocument> {
        val cachedValue = openIdMetadataCache.getIfPresent(OpenIdMetadataDocument::class.java)

        if (cachedValue != null) {
            return Future.succeededFuture(cachedValue)
        }

        val future: Future<OpenIdMetadataDocument> = Future.future<OpenIdMetadataDocument>()
        webClient.getAbs("https://login.botframework.com/v1/.well-known/openidconfiguration")
                .`as`(BodyCodec.jsonObject())
                .send({ ar ->
                    VertxUtils.wrap(ar, { resp ->
                        val result = resp.body().mapTo(OpenIdMetadataDocument::class.java)
                        openIdMetadataCache.put(OpenIdMetadataDocument::class.java, result)
                        future.complete(result)
                    }, { ex -> future.fail(ex) })
                })
        return future
    }

    private fun prepareSecondFuture(webClient: WebClient, openIdMetadataDocument: OpenIdMetadataDocument): Future<AllResults> {
        val cachedValue = signingKeysWrapperCache.getIfPresent(SigningKeysWrapper::class.java)

        if (cachedValue != null) {
            return Future.succeededFuture(AllResults(openIdMetadataDocument,cachedValue))
        }

        val future: Future<AllResults> = Future.future<AllResults>()
        webClient.getAbs(openIdMetadataDocument.jwksUri)
                .`as`(BodyCodec.jsonObject())
                .send({ ar ->
                    VertxUtils.wrap(ar, { resp ->
                        val result = resp.body().mapTo(SigningKeysWrapper::class.java)
                        signingKeysWrapperCache.put(SigningKeysWrapper::class.java, result)

                        val wrapper = resp.body().mapTo(SigningKeysWrapper::class.java)
                        future.complete(AllResults(openIdMetadataDocument,wrapper))
                    }, { ex -> future.fail(ex) })
                })
        return future
    }

    private data class AllResults(val openIdMetadataDocument: OpenIdMetadataDocument, val signingKeysWrapper: SigningKeysWrapper)

}