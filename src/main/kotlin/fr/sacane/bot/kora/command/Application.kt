package fr.sacane.bot.kora.command

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.nio.file.Path


fun getToken(): String?{
    val file = Path.of(System.getProperty("user.dir").plus( "/hidden.txt")).toFile()
    return try{
        BufferedReader(FileReader(file)).use {
            it.lines().findFirst().get()
        }
    }catch (e: IOException){
        null
    }

}

fun getIdTest(): String?{
    val file = Path.of(System.getProperty("user.dir").plus( "/hidden.txt")).toFile()
    return try {
        BufferedReader(FileReader(file)).use{
            it.lines().filter{
                it.startsWith("idTest=")
            }.findFirst().get().replace("idTest=", "")
        }
    }catch(e: IOException){
        null
    }
}

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
    val jda = JDABuilder.createDefault(getToken())
        .setActivity(Activity.listening("Samy chanter"))
        .addEventListeners(Kora(), Test())
        .build()
    println(getIdTest())
    jda.awaitReady()
    jda.getGuildById("1019661871923597452")?.upsertCommand("hello", "Say hello")?.queue()
}
