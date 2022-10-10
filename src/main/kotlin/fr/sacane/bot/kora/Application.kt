package fr.sacane.bot.kora

import fr.sacane.bot.kora.command.HeadsOrTailCommand
import fr.sacane.bot.kora.command.Kora
import fr.sacane.bot.kora.command.PollCommandListener
import fr.sacane.bot.kora.command.WriterRaceCommand
import fr.sacane.bot.kora.utils.Config
import fr.sacane.bot.kora.utils.Mode
import fr.sacane.bot.kora.utils.setUpCommands
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent




fun main(args: Array<String>) {
    val jda = JDABuilder.createDefault(Config.getToken())
        .setActivity(Activity.listening("Samy chanter"))
        .addEventListeners(Kora(), PollCommandListener(), WriterRaceCommand(), HeadsOrTailCommand())
        .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
        .build()
    jda.awaitReady()
//    jda.setUpCommands(Config.getId(Mode.TEST)!!)
    jda.setUpCommands(Config.getId(Mode.PROD)!!)
}
