package me.wolfity

import me.wolfity.cache.ChatCacheFlushScheduler
import me.wolfity.commands.ChatCommands
import me.wolfity.commands.UserCommandParameter
import me.wolfity.commands.UserParameterType
import me.wolfity.listeners.ChatListeners
import me.wolfity.listeners.PlayerListeners
import me.wolfity.manager.ChatManager
import me.wolfity.manager.ChatMessageManager
import me.wolfity.manager.PlayerManager
import me.wolfity.misc.UpdateChecker
import me.wolfity.sql.DatabaseManager
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.Lamp
import revxrsal.commands.bukkit.BukkitLamp
import revxrsal.commands.bukkit.actor.BukkitCommandActor

lateinit var plugin: SimpleChatMod

class SimpleChatMod : JavaPlugin() {

    companion object {
        const val RESOURCE_ID: Int = 125980
    }

    private lateinit var lamp: Lamp<BukkitCommandActor>
    private lateinit var _chatManager: ChatManager
    private lateinit var _playerManager: PlayerManager
    private lateinit var _chatMessageManager: ChatMessageManager

    private lateinit var cacheFlusher: ChatCacheFlushScheduler

    val playerManager: PlayerManager
        get() = _playerManager

    val chatMessageManager: ChatMessageManager
        get() = _chatMessageManager

    val chatManager: ChatManager
        get() = _chatManager

    override fun onEnable() {
        plugin = this
        updateCheck()

        saveDefaultConfig()
        setupLamp()
        registerManagers()
        registerListeners()
        registerCommands()

        this.cacheFlusher = ChatCacheFlushScheduler(chatMessageManager)
        cacheFlusher.start()


    }

    override fun onDisable() {
        cacheFlusher.shutdown()
    }

    private fun registerManagers() {
        DatabaseManager().init()
        this._chatManager = ChatManager(this)
        this._playerManager = PlayerManager()
        this._chatMessageManager = ChatMessageManager()
    }

    private fun registerListeners() {
        Bukkit.getPluginManager().registerEvents(ChatListeners(this), this)
        Bukkit.getPluginManager().registerEvents(PlayerListeners(playerManager), this)
    }

    private fun registerCommands() {
        lamp.register(ChatCommands(this))
    }

    private fun setupLamp() {
        this.lamp = BukkitLamp.builder(this)
            .parameterTypes {
                it.addParameterType(UserCommandParameter::class.java, UserParameterType())
            }
            .build()
    }

    private fun updateCheck() {
        UpdateChecker().getVersion { version ->
            if (this.description.version == version) {
                logger.info("There is not a new update available.");
            } else {
                logger.info("There is a new update available for Simple Chat Moderation");
            }
        }
    }
}
