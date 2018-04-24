package com.jgardo.skypebot.notification

import com.jgardo.skypebot.config.Config
import com.jgardo.skypebot.config.ConfigService
import com.jgardo.skypebot.message.MessageSender
import com.jgardo.skypebot.message.model.Message
import com.jgardo.skypebot.notification.model.Activity
import com.jgardo.skypebot.util.TextTranslator
import javax.inject.Inject

class NotificationController @Inject constructor(
        private val configService: ConfigService,
        private val messageSender: MessageSender,
        private val textTranslator: TextTranslator) {

    fun notify(activity: Activity) {
        val appId = configService.getString(Config.APP_ID)
        if (activity.type == "conversationUpdate"
                && activity.membersAdded != null && activity.membersAdded.isNotEmpty() && activity.membersAdded.first().id == appId) {
            val conversationId = activity.conversation.id
            val text = textTranslator.translate(com.jgardo.skypebot.config.Text.BOTS_INVITATION_ON_GROUP, hashMapOf("conversationId" to conversationId))
            val message = Message(conversationId = conversationId, message = text)

            messageSender.send(message)
        }
    }
}