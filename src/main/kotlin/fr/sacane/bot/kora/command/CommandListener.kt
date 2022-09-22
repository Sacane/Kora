package fr.sacane.bot.kora.command

import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands

class Kora: ListenerAdapter(){
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if(event.name != "hello") return
        println("good")
        event.reply("Hello ${event.user.name}").queue()
    }
}

class Test: ListenerAdapter(){
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if(event.name != "test") return
        event.reply("You're testing my command").queue()
    }
}

//class CommandGuildManager: ListenerAdapter(){
//    override fun onGuildReady(event: GuildReadyEvent) {
//        //List of commands
//        val commands = mutableListOf<CommandData>()
//        commands.add(Commands.slash())
//    }
//}