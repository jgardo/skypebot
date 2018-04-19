package com.jgardo.skypebot.notification

import com.jgardo.skypebot.message.model.Message
import com.jgardo.skypebot.notification.model.Activity
import io.vertx.core.eventbus.MessageProducer
import io.vertx.core.logging.LoggerFactory
import javax.inject.Inject
import javax.inject.Named

class NotificationController @Inject constructor(@Named("appId") private val appId : String, @Named("messageSender")private val messageSender: MessageProducer<Message>) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun notify(activity: Activity) {
        if (activity.type == "conversationUpdate"
                && activity.membersAdded != null && activity.membersAdded.isNotEmpty() && activity.membersAdded.first().id == appId) {
            val conversationId = activity.conversation.id
            val text = "Hi, this conversation id is: \"$conversationId\""
            val message = Message(conversationId = conversationId, message = text)

            messageSender.send(message)
        }
    }
}