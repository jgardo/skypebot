package com.jgardo.skypebot.notification.model

enum class Event(val eventName:String, val clazz:Class<*>) {
    NOTIFICATION("notification",Activity::class.java)
}