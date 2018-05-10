package com.jgardo.skypebot

import com.jgardo.skypebot.message.DirectMessageModule
import com.jgardo.skypebot.notification.NotificationModule

fun main(args : Array<String>) {
    SkypebotApplication(
            NotificationModule(),
            DirectMessageModule()
    ).run()
}