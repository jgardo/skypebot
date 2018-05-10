package com.jgardo.skypebot.greeting

enum class Text(override val code : String, override val defaultValue:String) : com.jgardo.skypebot.config.Text {
    BOTS_INVITATION_ON_GROUP("message.invitationOnGroup","Hi, id of this conversation is \":conversationId\".")
}