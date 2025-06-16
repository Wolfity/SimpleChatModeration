package me.wolfity.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class ChatSlowedEvent(val slowSeconds: Int): Event() {

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }
}