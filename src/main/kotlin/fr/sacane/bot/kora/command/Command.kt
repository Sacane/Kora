package fr.sacane.bot.kora.command

import net.dv8tion.jda.api.hooks.ListenerAdapter

abstract class Command : ListenerAdapter(){
    abstract fun sendError()
    abstract fun help()
}