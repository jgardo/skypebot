package com.jgardo.skypebot.message.exception

class ReceiverNotFoundException(receiverName : String) : RuntimeException("Receiver $receiverName not registered.")