package me.wolfity

import me.wolfity.commands.ChatCommands
import me.wolfity.listeners.ChatListeners
import me.wolfity.manager.ChatManager
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.Lamp
import revxrsal.commands.bukkit.BukkitLamp
import revxrsal.commands.bukkit.actor.BukkitCommandActor

lateinit var plugin: SimpleChatMod

class SimpleChatMod : JavaPlugin() {

    private lateinit var lamp: Lamp<BukkitCommandActor>
    private lateinit var _chatManager: ChatManager

    val chatManager: ChatManager
        get() = _chatManager

    override fun onEnable() {
        plugin = this

        saveDefaultConfig()

        this._chatManager = ChatManager(this)

        this.lamp = BukkitLamp.builder(this).build()
        lamp.register(ChatCommands(this))

        Bukkit.getPluginManager().registerEvents(ChatListeners(this), this)

    }

    override fun onDisable() {

    }
}
