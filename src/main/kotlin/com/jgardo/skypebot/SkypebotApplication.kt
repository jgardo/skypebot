package com.jgardo.skypebot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SkypebotApplication

fun main(args: Array<String>) {
    runApplication<SkypebotApplication>(*args)
}
