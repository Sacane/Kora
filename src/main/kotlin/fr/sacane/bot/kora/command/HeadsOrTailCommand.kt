package fr.sacane.bot.kora.command

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.FileUpload
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import kotlin.random.Random

class HeadsOrTailCommand : ListenerAdapter(){

    companion object{
        fun inputStream(heads: Boolean): InputStream? = Companion::class.java.classLoader.getResourceAsStream("assets/${if (heads) "head.png" else "tail.png"}")
    }
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if(event.name != "pf") return
        val heads = Random.nextBoolean()
        val inputStream = inputStream(heads)
        val outputStream = ByteArrayOutputStream()
        inputStream?.copyTo(outputStream)
        val fileData = outputStream.toByteArray()
        val tempFile = File.createTempFile("temp", ".png")
        tempFile.writeBytes(fileData)
        event.replyFiles(listOf(FileUpload.fromData(tempFile))).queue()
    }
}