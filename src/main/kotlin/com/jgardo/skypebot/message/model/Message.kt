package com.jgardo.skypebot.message.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class Message @JsonCreator constructor(
        @JsonProperty("receiver") val receiver : String?,
        @JsonProperty("conversationId") val conversationId : String?,
        @JsonProperty("message") val message: String)