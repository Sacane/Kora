package fr.sacane.bot.kora.command

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

class WriterRaceCommand: ListenerAdapter(){
    private var words: Words = mutableListOf()
    companion object{
        private val FILE = Companion::class.java.classLoader.getResourceAsStream("words.txt")
    }
    init{
        FILE?.bufferedReader()?.useLines { lines ->
            lines.forEach {
                words.add(it)
            }
        } ?: throw Exception("File not found")
    }
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if(event.name != "race") return

        val players: Players = mutableMapOf()

        players.addAllNotNull(
            List(5) {i -> event.getOption("player${i+1}")?.asUser.let { it?.id to RacePlayer(it?.id ?:"", it?.name ?: "")}}
        )
        if(players.containsKey(event.user.id)){
            players.remove(event.user.id)
        }
        players.addAllNotNull(event.user.id to RacePlayer(event.user.id, event.user.name))
        val sentence = words.buildSentence()
        RaceGame(event, players, sentence).start()

    }
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

fun Players.hasAllFinished(): Boolean{
    return this.values.all { it.hasFinished }
}



class RacePlayer(
    private val id: String,
    private val name: String
){

    private var startTimer: Long? = 0
    private var endTimer: Long? = 0
    var hasFinished = false
    fun startTimer(){
        this.startTimer = System.currentTimeMillis()
    }
    fun asMentionedUser(): String{
        return "<@$id>"
    }
    override fun toString(): String {
        return name
    }

    fun registerTime() {
        endTimer = System.currentTimeMillis()
        hasFinished = true
    }
    fun getScore(): Duration?{
        return if(endTimer != 0L){
            return (endTimer?.minus(startTimer!!))?.millisToDuration(DurationUnit.SECONDS)?.seconds
        } else null
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
        return this.players.containsKey(id)
    }
    private fun sendInvitationToPlay(){
        event.replyEmbeds(
            EmbedBuilder()
                .setTitle("Invitation au jeu du RaceGame")
                .setDescription("Les joueurs ${players.mentionAll()} sont invités à jouer")
                .addField("Phrase à réécrire", sentence, true)
                .build()
        ).addActionRow(
            Button.secondary("Bt_start_${gameID}", "START")
        ).queue{
            it.retrieveOriginal().queue { id -> mainID =  id.id}
            CoroutineScope(Dispatchers.IO).launch {
                delay(60_000)
                sendResults()
            }
            if(players.hasAllFinished()){
                sendResults()
            }
        }
    }
    private fun isPlaying(id: String): Boolean{
        return playings.contains(id)
    }
    override fun onMessageReceived(event: MessageReceivedEvent) {
        val userId = event.author.id
        if(isPartOfGame(userId) && isPlaying(userId)){
            if(sentence == event.message.contentRaw){
                val player = players[userId]
                player?.registerTime()
                refreshResults()
                event.channel.sendMessage("${players[userId]?.asMentionedUser()} a terminé !")
                playings.remove(userId)
                return
            } else {
                event.channel.sendMessage("${players[userId]?.asMentionedUser()} Dommage, essaye encore !").queue()
                return
            }
        }
        if(sentence == event.message.contentRaw && !isPartOfGame(userId)) {
            event.channel.sendMessage("<@${userId} Bien joué, mais tu ne fais pas partie du jeu").queue()
            return
        }
        if(isPartOfGame(userId) && !isPlaying(userId) && !players[userId]?.hasFinished!!){
            event.channel.sendMessage("${players[userId]?.asMentionedUser()} Tu as oublié d'appuyer sur le bouton start :(").queue()
        }
    }
    private fun refreshResults() {
        event.channel.editMessageEmbedsById(
            mainID,
            EmbedBuilder()
                .setTitle("Scores")
                .setDescription(sentence)
                .apply { players.forEach { addField(it.value.toString(), "${it.value.getScore() ?: "Pas de réponse"}", true)}}
                .build()
        ).queue()
    }
    private fun sendEnd(){
        event.channel.editMessageEmbedsById(
            mainID,
            EmbedBuilder()
                .setTitle("Le jeu est terminé !")
                .setDescription("Score finaux")
                .apply { players.forEach { addField(it.value.toString(), "${it.value.getScore() ?: "Pas de réponse"}", true)}}
                .build()
        ).queue()
        event.channel.editMessageComponentsById(
            mainID,
            ActionRow.of(
                Button.secondary("Bt_start_${gameID}", "START").asDisabled()
            )
        ).queue()
    }

//    private fun winner(): RacePlayer{
//        return players.values.toList().sortedBy { it.getScore() }.first()
//    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if(event.button.id != ("Bt_start_${gameID}")) return
        if(!isPartOfGame(event.user.id)) {
            event.reply("Vous ne faites pas partie des joueurs !").setEphemeral(true).queue()
            return
        }
        val userId = event.user.id
        if(!playings.contains(userId)){
            playings.add(userId)
            val player = players[userId]
            player?.startTimer() ?: return
            event.reply("Commencez à écrire !").setEphemeral(true).queue()
        } else {
            event.reply("Vous jouez déjà !").queue()
        }
    }
    fun start(){
        sendInvitationToPlay()
    }

    private fun sendResults(){
        event.jda.removeEventListener(this)
        sendEnd()
        event.channel.sendMessage("Le jeu est terminé !").queue()
    }
}



typealias Words=MutableList<String>
typealias Players = MutableMap<String?, RacePlayer>