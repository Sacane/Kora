package fr.sacane.bot.kora.command

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.FileUpload
import java.nio.file.Path
import kotlin.random.Random

class HeadsOrTailCommand : ListenerAdapter(){

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if(event.name != "pf") return

        val heads = Random.nextBoolean()
        val path = Path.of(System.getProperty("user.dir"), "assets", if(heads) "head.png" else "tail.png")
        val file = path.toFile()
        event.replyFiles(listOf(FileUpload.fromData(file))).queue()

    }

}