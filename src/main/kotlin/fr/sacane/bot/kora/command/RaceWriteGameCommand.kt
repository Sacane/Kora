package fr.sacane.bot.kora.command

import fr.sacane.bot.kora.exceptions.TooManyPlayerException
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class WriterRaceCommand: ListenerAdapter(){

    companion object{
        private var words: Words? = null
        private val FILE = File(System.getProperty("user.dir") + "/words.txt")
    }


    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if(event.name != "race") return

        if(words != null) {
            words?.addAllFileWords(FILE)
        } else return

        val players: Players = listOfNotNull(
            event.getOption("player1")?.asString?.let { Player(it) },
            event.getOption("player2")?.asString?.let { Player(it) }
        )

        //Récupérer la phrase à écrire

        //les personnes qui jouent

        //Récupérer le channel en cours

        //Afficher la phrase à écrire

        //Construire pour chaque joueur un embed message oui/non si il souhaite relever le défi
            //Si non il ne fait pas partie des joueurs
            //Si oui

        //démarrer le timer

        //Pour chaque joueur qui a accepté de jouer
            //Récupère le prochain message de ce joueur
            //Arrete son timer et afficher son résultat


    }
}

private fun Words.addAllFileWords(file: File){
    val reader = BufferedReader(FileReader(file))
    this.addAll(reader.lines().toList())
}

class Player(
    private val name: String
){

    private var startTimer: Duration = Duration.ZERO
    fun startTimer() : Unit{
        this.startTimer = System.currentTimeMillis().minutes
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
    private val players: Players
): ListenerAdapter(){

    private lateinit var uniqueID: String

    init {
        event.jda.addEventListener(this)
    }

    fun ask(){

    }

    fun start(){

    }

    private fun sendResults(){
        event.jda.removeEventListener(this)
    }
}



typealias Words=MutableList<String>
typealias Players = List<Player>