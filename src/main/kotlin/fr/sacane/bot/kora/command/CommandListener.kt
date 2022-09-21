package fr.sacane.bot.kora.command

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class Kora: ListenerAdapter(){
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if(event.name != "hello") return
        println("good")
        event.reply("Hello ${event.user.name}").queue()
    }
}

class Test:ListenerAdapter(){
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if(event.name != "test") return
        event.reply("You're testing my command").queue()
    }
}
