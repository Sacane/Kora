package fr.sacane.bot.kora

import kotlinx.coroutines.*
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
import java.time.Instant

class PollAdapter : ListenerAdapter(){

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if(event.name != "poll" || event.guild == null) return
//        sendPoll(event)
        Poll(event).create()
    }
}


class Poll(
    private val event: SlashCommandInteractionEvent,
    private val timeout: Long = 2L
): ListenerAdapter(){
    private val id: String = event.user.id + Instant.now()
    private lateinit var answers: MutableList<String>
    private lateinit var question: String
    private var answerResponses = mutableMapOf<String, Int>()
    private lateinit var currentId: String

    companion object{
        val logger: Logger = LoggerFactory.getLogger(Companion::class.java)
    }

    init {
        event.jda.addEventListener(this)
    }

    fun create(){
        sendModal()
    }

    private fun sendModal(){
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

        question = event.getValue("${event.member?.effectiveName}_question")?.asString.toString()
        val answers = event.getValue("${event.member?.effectiveName}_answer")?.asString
            ?.split(", ")

        if (answers != null) {
            this.answers = answers.toMutableList()
        } else {
            sendError()
        }
        sendPoll(event)
    }
    private fun sendPoll(event: ModalInteractionEvent){
        answers.forEachIndexed() { i, j -> answerResponses[('A' + i).toString()] = 0 }
        event.replyEmbeds(
            EmbedBuilder()
                .setTitle("${event.member?.effectiveName}")
                .setDescription("Sondage lancé par ${event.member?.effectiveName} : $question ?")
                .setFooter("Resultat du sondage dans $timeout minutes !")
                .setColor(Color.CYAN)
                .apply {
                    answers.forEachIndexed { i, name ->
                        addField("Réponse ${'A' + i}", name, true)
                    }
                }.build()
        ).addActionRow(
            List(answers.size) { i -> Button.primary(idTemplate(this.id, i), ('A' + i).toString()) }
        ).queue {
            it.retrieveOriginal().queue(){id -> currentId = id.id }
            CoroutineScope(Dispatchers.IO).launch{
                delay(timeout * 60_000)
                sendAnswer()
            }
        }

    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if( event.button.id == null || !event.button.id?.endsWith("_poll")!!) return
        event.reply("Votre vote à bien été pris en compte !").setEphemeral(true).queue()
        answerResponses.forEach { println(it) }
        println(event.button.label)
        answerResponses[event.button.label] = answerResponses[event.button.label]!! + 1


    }

    private fun sendAnswer() {
        event.jda.removeEventListener(this)
        event.channel.editMessageEmbedsById(currentId,
            EmbedBuilder()
                .setTitle("Résultats")
                .setDescription("Resulats du sondage de la question '$question'")
                .setFooter("Participants : [En cours de dev]")
                .apply {
                    answerResponses.forEach { (k, v) ->
                        addField(k, v.toString(), true)
                    }
                }.build()
        ).queue()
    }

    private fun idTemplate(id: String, i: Int): String{
        return "${id}.$i" + "_poll"
    }

    private fun sendError() {
        TODO("Not yet implemented")
    }
}

