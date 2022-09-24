package fr.sacane.bot.kora.command

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class WriterRaceCommand: ListenerAdapter(){
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if(event.name != "race") return

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

class Player(
    private val name: String,
    private val beginTimer: Long
)

class Race(
    private val event: SlashCommandInteractionEvent
): ListenerAdapter(){
    init {
        event.jda.addEventListener(this)
    }



    private fun sendResults(){
        event.jda.removeEventListener(this)
    }
}