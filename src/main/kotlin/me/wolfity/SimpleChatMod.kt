package me.wolfity

import me.wolfity.cache.ChatCacheFlushScheduler
import me.wolfity.commands.ChatCommands
import me.wolfity.commands.UserCommandParameter
import me.wolfity.commands.UserParameterType
import me.wolfity.gui.GUIListener
import me.wolfity.listeners.ChatListeners
import me.wolfity.player.PlayerListeners
import me.wolfity.manager.ChatStateManager
import me.wolfity.logging.ChatMessageManager
import me.wolfity.player.PlayerManager
import me.wolfity.misc.UpdateChecker
import me.wolfity.reports.ChatReportManager
import me.wolfity.db.DatabaseManager
import me.wolfity.files.CustomConfig
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
    private lateinit var _chatStateManager: ChatStateManager
    private lateinit var _playerManager: PlayerManager
    private lateinit var _chatMessageManager: ChatMessageManager
    private lateinit var _chatReportManager: ChatReportManager

    private lateinit var cacheFlusher: ChatCacheFlushScheduler

    val chatReportManager: ChatReportManager
        get() = _chatReportManager

    val playerManager: PlayerManager
        get() = _playerManager

    val chatMessageManager: ChatMessageManager
        get() = _chatMessageManager

    val chatStateManager: ChatStateManager
        get() = _chatStateManager

    lateinit var dbConfig: CustomConfig

    override fun onEnable() {
        plugin = this
        updateCheck()

        saveDefaultConfig()
        loadFiles()

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
        this._chatStateManager = ChatStateManager(this)
        this._playerManager = PlayerManager()
        this._chatMessageManager = ChatMessageManager()
        this._chatReportManager = ChatReportManager()
    }

    private fun registerListeners() {
        Bukkit.getPluginManager().registerEvents(ChatListeners(this), this)
        Bukkit.getPluginManager().registerEvents(PlayerListeners(playerManager), this)
        Bukkit.getPluginManager().registerEvents(GUIListener(), this)
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

    private fun loadFiles() {
        dbConfig = CustomConfig("db.yml")
    }

    private fun updateCheck() {
        UpdateChecker.getVersion { version ->
            if (this.description.version == version) {
                logger.info("There is not a new update available.");
            } else {
                logger.info("There is a new update available for Simple Chat Moderation");
            }
        }
    }
}
