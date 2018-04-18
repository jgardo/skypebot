package com.jgardo.skypebot.notification.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Activity @JsonCreator constructor(
        @JsonProperty("action") val action : String?,
        @JsonProperty("membersAdded") val membersAdded : List<Identificator>,
        @JsonProperty("type") val type : String,
        @JsonProperty("timestamp") val timestamp : String,
        @JsonProperty("id") val id : String,
        @JsonProperty("channelId") val channelId : String,
        @JsonProperty("serviceUrl") val serviceUrl : String,
        @JsonProperty("from") val from : Identificator,
        @JsonProperty("conversation") val conversation : Identificator,
        @JsonProperty("text") val text : String?,
        @JsonProperty("textFormat") val textFormat : String?,
        @JsonProperty("recipient") val recipient : Identificator


)