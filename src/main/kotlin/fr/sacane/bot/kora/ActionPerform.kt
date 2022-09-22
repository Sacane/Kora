package fr.sacane.bot.kora

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

class ButtonListener: ListenerAdapter(){
    companion object{
        val logger = LoggerFactory.getLogger(Companion::class.java)
    }
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        logger.info("A button was pressed : ${event.button.label}")
    }
}