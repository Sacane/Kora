package fr.sacane.bot.kora

import fr.sacane.bot.kora.utils.Config
import fr.sacane.bot.kora.utils.Mode
import fr.sacane.bot.kora.utils.setUpCommands
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.nio.file.Path




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

fun main(args: Array<String>) {
    val jda = JDABuilder.createDefault(Config.getToken())
        .setActivity(Activity.listening("Samy chanter"))
        .addEventListeners(Kora(), Test())
        .build()
    jda.awaitReady()
    jda.setUpCommands(Config.getId(Mode.TEST)!!)
    jda.setUpCommands(Config.getId(Mode.PROD)!!)
}
