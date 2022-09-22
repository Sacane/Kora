package fr.sacane.bot.kora.command

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.hooks.ListenerAdapter
class Kora: ListenerAdapter(){
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if(event.name != "hello") return
        println("good")
        event.reply("Hello ${event.user.name}").queue()
    }
}

class Test: ListenerAdapter(){
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if(event.name != "test") return
        val menu = SelectMenu.create("menu:class")
            .setPlaceholder("Choose your questions")
            .setRequiredRange(1, 1)
            .addOption("Question 1", "Ceci ?")
            .addOption("Question 2", "Cela ?")
            .addOption("Question 2", "Ou bien ça ?")
            .build()

        event.reply("Choisissez votre réponse :")
            .setEphemeral(true)
            .addActionRow(menu)
            .queue()
    }

}
