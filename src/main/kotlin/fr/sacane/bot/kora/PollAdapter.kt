package fr.sacane.bot.kora

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle


import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color

class PollAdapter : ListenerAdapter(){


    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if(event.name != "poll" || event.guild == null) return
        sendPoll(event)
    }




    private fun sendPoll(event: SlashCommandInteractionEvent){
        val options = mutableListOf("oui", "non")
        val uniqueId = event.member?.effectiveName

        val option = event.getOption("a")?.asString
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



class Form(): ListenerAdapter(){

    companion object{
        val logger: Logger = LoggerFactory.getLogger(Companion::class.java)
    }
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if(event.name != "form") return
        val questionInput = TextInput.create("${event.member?.effectiveName}_question", "Question", TextInputStyle.SHORT)
            .setMinLength(1)
            .setRequired(true)
            .build()

        val answerInput = TextInput.create("${event.member?.effectiveName}_answer", "Answers", TextInputStyle.PARAGRAPH)
            .setMinLength(3)
            .setRequired(true)
            .build()

        val modal = Modal.create("${event.member?.effectiveName}_modal", "Construisez votre question !")
            .addActionRows(listOf(ActionRow.of(questionInput), ActionRow.of(answerInput)))
            .build()

        event.replyModal(modal).queue()
    }


    override fun onModalInteraction(event: ModalInteractionEvent) {
        if(event.modalId != "${event.member?.effectiveName}_modal") return

        val question = event.getValue("${event.member?.effectiveName}_question")?.asString
        val answers = event.getValue("${event.member?.effectiveName}_answer")?.asString
            ?.split(", ")



        event.reply(answers.toString()).queue()

    }

    private fun sendError(){

    }

    private fun help(){

    }
}

