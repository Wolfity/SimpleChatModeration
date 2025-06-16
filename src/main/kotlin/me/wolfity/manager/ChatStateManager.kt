package me.wolfity.manager

import me.wolfity.SimpleChatMod
import me.wolfity.events.ChatMutedEvent
import me.wolfity.events.ChatSlowedEvent
import me.wolfity.plugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class ChatStateManager(plugin: SimpleChatMod) {

    private var _chatMuted: Boolean = false
    private var _chatSlowSeconds: Int = 0

    val chatSlowSeconds: Int
        get() = _chatSlowSeconds.coerceAtLeast(0)

    init {
        val shouldPersist = plugin.config.getBoolean("persist-mute-through-restarts")
        if (shouldPersist) {
            this._chatMuted = plugin.config.getBoolean("chat-muted")
        }
        this._chatSlowSeconds = plugin.config.getInt("chat-slow-seconds")
    }

    val chatMuted: Boolean
        get() = _chatMuted

    fun toggleChatMuted(initiator: Player) {
        this._chatMuted = !_chatMuted
        plugin.config.set("chat-muted", _chatMuted)
        plugin.saveConfig()
        Bukkit.getPluginManager().callEvent(ChatMutedEvent(_chatMuted, initiator))
    }

    fun slowChat(seconds: Int) {
        val safeSeconds = seconds.coerceAtLeast(0)
        this._chatSlowSeconds = safeSeconds
        plugin.config.set("chat-slow-seconds", safeSeconds)
        plugin.saveConfig()
        Bukkit.getPluginManager().callEvent(ChatSlowedEvent(_chatSlowSeconds))
    }

    fun resetSlow()  {
        slowChat(0)
    }
}