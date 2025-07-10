package me.wolfity

import me.wolfity.cache.ChatCacheFlushScheduler
import me.wolfity.cache.ChatMessageCache
import me.wolfity.commands.ChatCommands
import me.wolfity.commands.FilterCommands
import me.wolfity.commands.LampExceptionHandler
import me.wolfity.commands.UserCommandParameter
import me.wolfity.commands.UserParameterType
import me.wolfity.commands.WordSuggestions
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
import me.wolfity.filter.ChatFilter
import me.wolfity.util.style
import me.wolfity.webhook.WebhookManager
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.Lamp
import revxrsal.commands.bukkit.BukkitLamp
import revxrsal.commands.bukkit.BukkitLampConfig
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
    private lateinit var _chatMessageCache: ChatMessageCache
    private lateinit var cacheFlusher: ChatCacheFlushScheduler
    private lateinit var _webhookManager: WebhookManager
    private lateinit var _chatFilter: ChatFilter

    val chatReportManager: ChatReportManager
        get() = _chatReportManager

    val playerManager: PlayerManager
        get() = _playerManager

    val chatMessageManager: ChatMessageManager
        get() = _chatMessageManager

    val chatStateManager: ChatStateManager
        get() = _chatStateManager

    val chatMessageCache: ChatMessageCache
        get() = _chatMessageCache

    val webhookManager: WebhookManager
        get() = _webhookManager

    val chatFilter: ChatFilter
        get() = _chatFilter

    lateinit var dbConfig: CustomConfig
    lateinit var filteredWordsConfig: CustomConfig

    override fun onEnable() {
        plugin = this
        updateCheck()

        saveDefaultConfig()
        loadFiles()

        setupLamp()
        registerManagers()
        registerListeners()
        registerCommands()

        this._chatFilter = ChatFilter(filteredWordsConfig.getStringList("filtered-words"))

        this.cacheFlusher = ChatCacheFlushScheduler(chatMessageManager)
        cacheFlusher.start()
    }

    override fun onDisable() {
        cacheFlusher.shutdown()
    }

    private fun registerManagers() {
        DatabaseManager().init()
        this._chatMessageCache = ChatMessageCache()
        this._chatStateManager = ChatStateManager(this)
        this._playerManager = PlayerManager()
        this._chatMessageManager = ChatMessageManager()
        this._chatReportManager = ChatReportManager()
        this._webhookManager = WebhookManager()
    }

    private fun registerListeners() {
        Bukkit.getPluginManager().registerEvents(ChatListeners(this), this)
        Bukkit.getPluginManager().registerEvents(PlayerListeners(playerManager), this)
        Bukkit.getPluginManager().registerEvents(GUIListener(), this)
    }

    private fun registerCommands() {
        lamp.register(ChatCommands(this))
        lamp.register(FilterCommands())
    }

    private fun setupLamp() {
        val lampConfig: BukkitLampConfig<BukkitCommandActor> = BukkitLampConfig.builder<BukkitCommandActor>(this)
            .disableBrigadier()
            .enableAsyncCompletion()
            .build()

        this.lamp = BukkitLamp.builder(lampConfig)
            .parameterTypes {
                it.addParameterType(UserCommandParameter::class.java, UserParameterType())
            }
            .exceptionHandler(LampExceptionHandler())
            .build()
    }

    private fun loadFiles() {
        dbConfig = CustomConfig("db.yml")
        filteredWordsConfig = CustomConfig("filtered-words.yml")
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
