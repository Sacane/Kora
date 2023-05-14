package fr.sacane.bot.kora.utils

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.io.BufferedReader
import java.io.FileReader
import java.nio.file.Path

fun JDA.setUpCommands(guildId: String) {
    this.addCommandQueueWithOption(guildId,
        "poll",
        "Lancement d'un poll",
        mutableListOf(
            OptionData(OptionType.INTEGER, "seconds", "Durée en secondes du sondage"),
            OptionData(OptionType.INTEGER, "minutes", "Durée en minutes du sondage"),
            OptionData(OptionType.INTEGER, "hours", "Durée en heures du sondage"),
            OptionData(OptionType.INTEGER, "days", "Durée en jours du sondage"),
        ),
    )
    this.addCommandQueueWithOption(guildId, "race", "Lance le jeu 'writer race' !",
        mutableListOf(
            OptionData(OptionType.USER, "player1", "1er joueur"),
            OptionData(OptionType.USER, "player2", "2e joueur"),
            OptionData(OptionType.USER, "player3", "3e joueur"),
            OptionData(OptionType.USER, "player4", "4e joueur"),
            OptionData(OptionType.USER, "player5", "5e joueur")
        )
    )
    this.addCommandQueue(guildId, "pf", "Lance une pièce !")
}

fun JDA.addCommandQueue(guildId: String, name: String, description: String){
    this.getGuildById(guildId)?.upsertCommand(name, description)?.queue()
}

fun JDA.addCommandQueueWithOption(guildId: String, name: String, description: String, options: MutableList<OptionData>){
    this.getGuildById(guildId)?.upsertCommand(name, description)?.addOptions(options)?.queue()
}

enum class Mode{
    TEST, PROD
}
class Config {

    companion object{
        private val file = Companion::class.java.classLoader.getResourceAsStream("hidden.txt")
        fun getToken(): String?{
            return file?.bufferedReader().use {
                    it?.lines()?.findFirst()?.get()
                }
        }
        fun getId(mode: Mode): String?{
            return when(mode){
                Mode.TEST -> getIdTest()
                Mode.PROD -> getIdFriends()
            }
        }
        private fun getIdTest(): String?{
            val file = Path.of(System.getProperty("user.dir").plus( "/hidden.txt")).toFile()
            return BufferedReader(FileReader(file)).use{ it ->
                    it.lines().filter{
                        it.startsWith("idTest=")
                    }.findFirst().get().replace("idTest=", "")
                }
        }
        private fun getIdFriends(): String?{
            return file?.bufferedReader().use{ it ->
                it?.lines()?.filter{
                        it.startsWith("id=")
                    }?.findFirst()?.get()?.replace("id=", "")
                }

        }
    }
}