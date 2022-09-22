package fr.sacane.bot.kora

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.requests.GatewayIntent
import org.slf4j.LoggerFactory
import java.awt.Color

class PollAdapter : ListenerAdapter(){

    companion object{
        val logger = LoggerFactory.getLogger(Companion::class.java.name)
    }

    private lateinit var options: Array<String>

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if(event.name != "poll" || event.guild == null) return
        logger.info("${event.member?.effectiveName} start a poll")
        sendPoll(event)
    }

    override fun onGenericCommandInteraction(event: GenericCommandInteractionEvent) {
        logger.info(event.subcommandName)
    }

    private fun sendPoll(event: SlashCommandInteractionEvent){
        val options = mutableListOf("oui", "non")
        val uniqueId = event.member?.effectiveName

        val option = event.getOption("a")?.asString
        logger.info("option morning : $option")


        event.replyEmbeds(
            EmbedBuilder()
                .setTitle("${event.member?.effectiveName}")
                .setDescription("Sondage lancé par ${event.member?.effectiveName}")
                .setFooter("Resultat du sondage bientot")
                .setColor(Color.CYAN)
                .apply {
                    options.forEachIndexed { i, name ->
                        addField("Réponse ${'A' + i}", name, true)
                    }
                }.build()
        ).addActionRow(
            List(options.size) { i -> Button.primary("poll.$uniqueId.$i", ('A' + i).toString()) }
        ).queue {
            it.retrieveOriginal().queue()
        }
    }

    private fun successEmbed(message: String): MessageEmbed = EmbedBuilder()
        .setTitle("Succès")
        .setDescription(message)
        .setColor(Color.GREEN)
        .build()

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        event.replyEmbeds(successEmbed("Merci ! Ton vote à bien été pris en compte")).setEphemeral(true).queue()
    }
}