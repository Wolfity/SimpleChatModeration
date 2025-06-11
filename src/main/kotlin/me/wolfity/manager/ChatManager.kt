package me.wolfity.manager

import me.wolfity.SimpleChatMod
import me.wolfity.events.ChatMutedEvent
import me.wolfity.plugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class ChatManager(plugin: SimpleChatMod) {

    private var _chatMuted: Boolean = false

    init {
        val shouldPersist = plugin.config.getBoolean("persist-mute-through-restarts")
        if (shouldPersist) {
            this._chatMuted = plugin.config.getBoolean("chat-muted")
        }
    }

    val chatMuted: Boolean
        get() = _chatMuted

    fun toggleChatMuted(initiator: Player) {
        this._chatMuted = !_chatMuted
        plugin.config.set("chat-muted", _chatMuted)
        plugin.saveConfig()
        Bukkit.getPluginManager().callEvent(ChatMutedEvent(_chatMuted, initiator))
    }
}