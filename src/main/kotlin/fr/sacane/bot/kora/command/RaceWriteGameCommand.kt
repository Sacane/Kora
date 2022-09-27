package fr.sacane.bot.kora.command

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class WriterRaceCommand: ListenerAdapter(){
    private var words: Words = mutableListOf()
    companion object{

        private val FILE = File(System.getProperty("user.dir") + "/words.txt")
    }
    init{
        words.addAllFileWords(FILE)
    }
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if(event.name != "race") return

        val players: Players = mutableMapOf(
            Pair(event.user.id.toLong(), Player(event.user.id, event.user.name)),
            event.getOption("player1")?.asUser.let { Pair(it?.id!!.toLong(), Player(it.id, it.name)) },
            event.getOption("player2")?.asUser.let { Pair(it?.id!!.toLong(), Player(it.id, it.name)) },
            event.getOption("player3")?.asUser.let { Pair(it?.id!!.toLong(), Player(it.id, it.name)) },
            event.getOption("player4")?.asUser.let { Pair(it?.id!!.toLong(), Player(it.id, it.name)) },
            event.getOption("player5")?.asUser.let { Pair(it?.id!!.toLong(), Player(it.id, it.name)) }
        )
        val sentence = words.buildSentence()
        RaceGame(event, players, sentence).start()

    }
}

private fun Words.addAllFileWords(file: File){
    val reader = BufferedReader(FileReader(file))
    this.addAll(reader.lines().toList())
}

private fun Words.buildSentence(): String{
    val builder: StringBuilder = StringBuilder()
    for(i in 0..12){
        builder.append(random()).append(" ")
    }
    return builder.append(random()).toString()
}

fun Players.mentionAll(): String{
    val builder = StringBuilder()
    for (player in this){
        builder.append(player.value.asMentionedUser()).append(" ")
    }
    return builder.toString()
}



class Player(
    private val id: String,
    private val name: String
){

    private var startTimer: Duration = Duration.ZERO
    fun startTimer() : Unit{
        this.startTimer = System.currentTimeMillis().minutes
    }
    fun asMentionedUser(): String{
        return "<@$id>"
    }
    override fun toString(): String {
        return "Joueur $name"
    }

    fun computeEndTime(): Duration {
        return System.currentTimeMillis().minutes - startTimer
    }
}

class RaceGame(
    private val event: SlashCommandInteractionEvent,
    private val players: Players,
    private val sentence: String
): ListenerAdapter(){

    private var gameID: String = "race_game_${Instant.now()}"
    private var playings: MutableSet<String> = mutableSetOf()
    private lateinit var mainID: String

    init {
        event.jda.addEventListener(this)

    }

    private fun isPartOfGame(id: String): Boolean{
        return this.players.containsKey(id.toLong())
    }
    private fun sendInviteToGame(){
//        event.reply(players.mentionAll()).queue()
        event.replyEmbeds(
            EmbedBuilder()
                .setTitle("Invitation au jeu du RaceGame")
                .setDescription("Les joueurs ${players.mentionAll()} sont invités à jouer")
                .addField("Phrase à réécrire", sentence, true)
                .build()
        ).addActionRow(
            Button.secondary("poll_button_${gameID}", "START")
        ).queue{
            it.retrieveOriginal().queue { id -> mainID =  id.id}
            CoroutineScope(Dispatchers.IO).launch {
                delay(60_000)
                sendResults()
            }
        }
    }



    fun sendScores(){

    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {

    }

    private fun isPlaying(id: String): Boolean{
        return playings.contains(id)
    }
    private fun startPlayerGame(id: Long){
        val player = players[event.id.toLong()]
        player?.startTimer() ?: return
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val userId = event.author.id
        if(isPartOfGame(userId) && isPlaying(userId)){
            if(sentence == event.message.contentRaw){
                updateResult(userId)
            }
        }
    }

    private fun updateResult(userId: String) {
        event.channel.editMessageEmbedsById(
            mainID,
            //TODO finish it
        )
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if(!isPartOfGame(event.id)) {
            event.reply("Vous ne faites pas partie des joueurs !").setEphemeral(true).queue()
            return
        }
        if(!playings.contains(event.id)){
            playings.add(event.id)
            val player = players[event.id.toLong()]
            player?.startTimer() ?: return

        } else {
            event.reply("Vous jouez déjà !")
        }
    }
    fun start(){
        sendInviteToGame()
    }

    private fun sendResults(){
        event.jda.removeEventListener(this)
    }
}



typealias Words=MutableList<String>
typealias Players = Map<Long, Player>