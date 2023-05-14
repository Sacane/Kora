package fr.sacane.bot.kora.command

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class Kora: ListenerAdapter(){
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if(event.name != "hello") return
        event.reply("Hello ${event.user.name}").queue()
    }
}