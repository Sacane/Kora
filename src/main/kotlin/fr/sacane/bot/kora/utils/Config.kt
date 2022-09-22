package fr.sacane.bot.kora.utils

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.nio.file.Path

fun JDA.setUpCommands(guildId: String){
    this.addCommandQueue(guildId, "hello", "Dites 'hello' au bot")
    this.addCommandQueue(guildId, "test", "Ceci est un test")
    this.addCommandQueue(guildId, "poll", "Lancement d'un poll")
}

fun JDA.addCommandQueue(guildId: String, name: String, description: String){
    this.getGuildById(guildId)?.upsertCommand(name, description)?.queue()
}

fun JDA.addCommandQueueWithOption(guildId: String, name: String, description: String, options: MutableList<OptionData>){
    this.getGuildById(guildId)?.upsertCommand(name, description)?.addOptions(options)
}

enum class Mode{
    TEST, PROD
}
class Config {



    companion object{
        fun getToken(): String?{
            val file = Path.of(System.getProperty("user.dir").plus( "/hidden.txt")).toFile()
            return try{
                BufferedReader(FileReader(file)).use {
                    it.lines().findFirst().get()
                }
            }catch (e: IOException){
                null
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
            return try {
                BufferedReader(FileReader(file)).use{
                    it.lines().filter{
                        it.startsWith("idTest=")
                    }.findFirst().get().replace("idTest=", "")
                }
            }catch(e: IOException){
                null
            }
        }
        private fun getIdFriends(): String?{
            val file = Path.of(System.getProperty("user.dir").plus( "/hidden.txt")).toFile()
            return try {
                BufferedReader(FileReader(file)).use{
                    it.lines().filter{
                        it.startsWith("id=")
                    }.findFirst().get().replace("id=", "")
                }
            }catch(e: IOException){
                null
            }
        }
    }
}