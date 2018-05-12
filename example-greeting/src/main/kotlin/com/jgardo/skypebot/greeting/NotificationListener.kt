package com.jgardo.skypebot.greeting

import com.jgardo.skypebot.config.BasicConfig
import com.jgardo.skypebot.config.ConfigService
import com.jgardo.skypebot.message.MessageSender
import com.jgardo.skypebot.message.model.Message
import com.jgardo.skypebot.notification.model.Activity
import com.jgardo.skypebot.notification.model.Event
import com.jgardo.skypebot.util.TextTranslator
import com.jgardo.skypebot.vertx.VertxConfigurer
import io.vertx.core.Vertx
import javax.inject.Inject

class NotificationListener @Inject constructor(
        private val configService: ConfigService,
        private val messageSender: MessageSender,
        private val textTranslator: TextTranslator) : VertxConfigurer {

    lateinit var vertx : Vertx

    override fun configure(vertx: Vertx) {
        vertx.eventBus().consumer<Activity>(Event.NOTIFICATION.eventName, { activity -> notify(activity.body())})
        this.vertx = vertx
    }

    private fun notify(activity: Activity) {
        val appId = configService.getString(BasicConfig.APP_ID)
        if (wasAddedToPrivateConversation(activity)
                || wasAddedToGroup(activity, appId)) {
            val conversationId = activity.conversation.id
            val text = textTranslator.translate(Text.BOTS_INVITATION_ON_GROUP, hashMapOf("conversationId" to conversationId))
            val message = Message(conversationId = conversationId, message = text)

            vertx.setTimer(4000, {
                messageSender.send(message)
            })
        }
    }

    private fun wasAddedToGroup(activity: Activity, appId: String?): Boolean {
        return (activity.type == "conversationUpdate"
                && activity.membersAdded?.isNotEmpty() ?: false
                && activity.membersAdded!!.first().id.contains(appId!!))
    }

    private fun wasAddedToPrivateConversation(activity: Activity): Boolean {
        return (activity.type == "contactRelationUpdate"
                && activity.action == "add")
    }

}