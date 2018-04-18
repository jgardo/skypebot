package com.jgardo.skypebot.notification.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class Identificator @JsonCreator constructor (
        @JsonProperty("id") val id : String,
        @JsonProperty("name") val name : String? = null
)