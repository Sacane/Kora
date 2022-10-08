package fr.sacane.bot.kora.command

import kotlinx.coroutines.*
import net.dv8tion.jda.api.EmbedBuilder
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
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

class PollCommandListener : ListenerAdapter(){

    private var numberPolls = 0
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if(event.name == "polls"){
            event.reply("number of active polls : $numberPolls")
            return
        }
        if(event.name != "poll" || event.guild == null) return

        val optionsTime = mutableMapOf<Long, DurationUnit>()
        optionsTime.addAllNotNull(
            event.getOption("seconds")?.asLong to DurationUnit.SECONDS,
            event.getOption("minutes")?.asLong to DurationUnit.MINUTES,
            event.getOption("hours")?.asLong to DurationUnit.HOURS,
            event.getOption("days")?.asLong to DurationUnit.DAYS
        )

        if(optionsTime.isEmpty() || optionsTime.size > 1){
            PollActionListener(event).reply()
            numberPolls =+ 1
        } else {
            val duration = optionsTime.values.first()
            PollActionListener(event, optionsTime.keys.first().timeByDuration(duration)!!, true, duration).reply()
            numberPolls += 1
        }
    }
}



fun Long.timeByDuration(duration: DurationUnit): Long?{
    return when(duration){
        DurationUnit.SECONDS ->  this.seconds.toLong(DurationUnit.MILLISECONDS)
        DurationUnit.MINUTES -> this.minutes.toLong(DurationUnit.MILLISECONDS)
        DurationUnit.HOURS -> this.hours.toLong(DurationUnit.MILLISECONDS)
        DurationUnit.DAYS -> this.days.toLong(DurationUnit.MILLISECONDS)
        else -> null
    }
}

fun Long.millisToDuration(duration: DurationUnit): Long?{
    return when(duration){
        DurationUnit.SECONDS -> this.milliseconds.toLong(duration)
        DurationUnit.MINUTES -> this.milliseconds.toLong(duration)
        DurationUnit.HOURS -> this.milliseconds.toLong(duration)
        DurationUnit.DAYS -> this.milliseconds.toLong(duration)
        else -> null
    }
}

class PollActionListener(
    private val event: SlashCommandInteractionEvent,
    private val timeout: Long = 1L,
    private val hasUnit: Boolean = false,
    private val duration: DurationUnit = DurationUnit.MINUTES
): ListenerAdapter(){

    private val id: String = event.user.id + Instant.now()
    private var answerResponses = mutableMapOf<String, Int>()
    private val userVote = mutableSetOf<String>()
    private val rearrangedAnswers = mutableMapOf<String, String>()
    private val unit = duration.name.lowercase()
    private val answerToUser = mutableMapOf<String, MutableList<String>>()

    private lateinit var answers: MutableList<String>
    private lateinit var question: String
    private lateinit var currentId: String

    companion object{
        val logger: Logger = LoggerFactory.getLogger(Companion::class.java)
    }

    init {
        event.jda.addEventListener(this)
    }

    fun reply(){
        sendModal()
    }

    private fun sendModal(){
        val questionInput = TextInput.create("${event.member?.effectiveName}_${id}_question", "Question", TextInputStyle.SHORT)
            .setMinLength(1)
            .setRequired(true)
            .build()

        val answerInput = TextInput.create("${event.member?.effectiveName}_${id}_answer", "Answers", TextInputStyle.PARAGRAPH)
            .setMinLength(3)
            .setRequired(true)
            .build()

        val modal = Modal.create("${event.member?.effectiveName}_${id}_modal", "Construisez votre question !")
            .addActionRows(listOf(ActionRow.of(questionInput), ActionRow.of(answerInput)))
            .build()

        event.replyModal(modal).queue()
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        if(event.modalId != "${event.member?.effectiveName}_${id}_modal") return

        question = event.getValue("${event.member?.effectiveName}_${id}_question")?.asString.toString()
        val answers = event.getValue("${event.member?.effectiveName}_${id}_answer")?.asString
            ?.split(", ")

        if (answers != null && answers.size < 6) {
            this.answers = answers.toMutableList()
        } else {
            sendError(event)
            return
        }
        sendPoll(event)
    }

    private fun sendPoll(event: ModalInteractionEvent){
        answers.forEachIndexed { i, answer ->
            run {
                answerResponses[('A' + i).toString()] = 0
                rearrangedAnswers[('A' + i).toString()] = answer
                answerToUser[('A' + i).toString() + " : $answer"] = mutableListOf()
            }
        }
        event.replyEmbeds(
            EmbedBuilder()
                .setTitle("Sondage lancé par ${event.member?.effectiveName}")
                .setDescription("$question ?")
                .setFooter("Resultat du sondage dans ${timeout.millisToDuration(duration)} $unit !")
                .setColor(Color.CYAN)
                .apply {
                    answers.forEachIndexed { i, name ->
                        addField("Réponse ${'A' + i}", name, true)
                    }
                }.build()
        ).addActionRow(
            List(answers.size) { i -> Button.primary("poll_button_${id}_$i", ('A' + i).toString()) }
        ).queue {
            it.retrieveOriginal().queue { id -> currentId = id.id }
            CoroutineScope(Dispatchers.IO).launch{
                delay(timeout * if(!hasUnit) 60_000 else 1)
                sendAnswer()
            }
        }

    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if( event.button.id == null || !event.button.id?.startsWith("poll_button_${id}")!!) return
        if(userVote.contains(event.member?.effectiveName)){
            event.reply("Vous avez déjà voter !")
            return
        }
        event.member?.let { userVote.add(it.effectiveName) }
        event.reply("Votre vote à bien été pris en compte !").setEphemeral(true).queue()
        answerResponses[event.button.label] = answerResponses[event.button.label]!! + 1
        event.member?.effectiveName?.let { answerToUser["${event.button.label} : ${rearrangedAnswers[event.button.label]}"]?.add(it) }
        editEmbed()
    }

    private fun editEmbed() {
        event.channel.editMessageEmbedsById(
            currentId,
            EmbedBuilder()
                .setTitle("Mise à jour")
                .setDescription("Question : '$question'")
                .apply {
                    answerToUser.forEach {
                        (k, v) -> addField(k, v.toString(), true).build()
                    }
                }.build()
        ).queue()
    }

    private fun sendAnswer() {
        event.jda.removeEventListener(this)
        event.channel.editMessageEmbedsById(currentId,
            EmbedBuilder()
                .setTitle("Sondage terminé ! Résultats finaux")
                .setDescription("Question : '$question'")
                .apply {
                    answerToUser.forEach {
                            (k, v) -> addField(k, v.toString(), true).build()
                    }
                }.build()
        ).queue()
        event.channel.editMessageComponentsById(
            currentId,
            ActionRow.of(
                List(answers.size) { i -> Button.primary("poll_button_${id}_$i", ('A' + i).toString()).asDisabled() }
            )
        ).queue()
    }

    private fun sendError(event: ModalInteractionEvent) {
        this.event.jda.removeEventListener(this)
        event.reply("Désolé, Un sondage ne peut contenir plus de 5 réponses :(").setEphemeral(true).queue()
    }
}

