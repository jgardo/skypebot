package com.jgardo.skypebot.notification.authorization

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenIdMetadataDocument @JsonCreator constructor(
    @JsonProperty("issuer") val issuer : String,
    @JsonProperty("authorization_endpoint") val authorizationEndpoint : String,
    @JsonProperty("jwks_uri") val jwksUri : String,
    @JsonProperty("id_token_signing_alg_values_supported") val idTokenSigningAlgValuesSupported : List<String>,
    @JsonProperty("token_endpoint_auth_methods_supported") val tokenEndpointAuthMethodsSupported : List<String>
)