package com.jgardo.skypebot.notification.authorization.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SigningKey @JsonCreator constructor(
        @JsonProperty("kty") val kty : String,
        @JsonProperty("use") val use : String,
        @JsonProperty("kid") val kid : String,
        @JsonProperty("x5t") val x5t : String,
        @JsonProperty("n") val n : String,
        @JsonProperty("x5c") val x5c : List<String>,
        @JsonProperty("endorsements") val endorsements : List<String>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SigningKeysWrapper @JsonCreator constructor(
        @JsonProperty("keys") val keys: List<SigningKey>
)