package me.wolfity.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import me.wolfity.SimpleChatMod
import me.wolfity.cache.ChatMessageCache
import me.wolfity.constants.Permissions
import me.wolfity.events.ChatMutedEvent
import me.wolfity.events.ChatSlowedEvent
import me.wolfity.filter.ChatFilter
import me.wolfity.filter.WordMatch
import me.wolfity.logging.ChatMessage
import me.wolfity.util.miniMessage
import me.wolfity.util.sendStyled
import me.wolfity.util.style
import me.wolfity.webhook.WebhookManager
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ChatListeners(private val plugin: SimpleChatMod) : Listener {

    private val filter = ChatFilter(plugin.config.getStringList("filtered-words"))

    @EventHandler
    fun onChatMuted(event: ChatMutedEvent) {
        val configMessage = if (event.muted) "chat-mute-message" else "chat-unmute-message"
        val message = plugin.config.getString(configMessage)!!
            .replace("{player}", miniMessage.serialize(event.initiator.displayName()))
        Bukkit.broadcast(style(message))
    }

    @EventHandler
    fun onChatSlowed(event: ChatSlowedEvent) {
        val messagePath = if (event.slowSeconds == 0) "chat-slow-reset" else "chat-slow-announcement"

        Bukkit.broadcast(style(plugin.config.getString(messagePath)!!.replace("{time}", event.slowSeconds.toString())))
    }

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        val player = event.player
        val isChatMuted = plugin.chatStateManager.chatMuted
        val message = event.message()

        if (isChatMuted && !player.hasPermission(Permissions.MUTE_CHAT_BYPASS_PERMISSION)) {
            event.isCancelled = true
            player.sendStyled(plugin.config.getString("chat-muted-message")!!)
            return
        }

        val rawMessage = miniMessage.serialize(message)
        val containsFiltered = filter.containsFilteredWord(rawMessage)

        if (containsFiltered && !player.hasPermission(Permissions.CHAT_FILTER_BYPASS_PERMISSION)) {
            handleFiltering(event, rawMessage, player)
        }
        handleSlow(event, player)

        if (event.isCancelled) return

        val pureContent = PlainTextComponentSerializer.plainText().serialize(message)
        plugin.chatMessageCache.addMessage(ChatMessage(sender = player.uniqueId, content = pureContent))
    }

    private fun handleSlow(event: AsyncChatEvent, player: Player) {
        val slowSeconds = plugin.chatStateManager.chatSlowSeconds
        if (slowSeconds > 0 && !player.hasPermission(Permissions.SLOW_CHAT_BYPASS)) {
            val lastMessages = plugin.chatMessageCache.getMessages(player.uniqueId)
            val lastMessageTime = lastMessages?.maxByOrNull { it.timestamp }?.timestamp ?: return
            val now = System.currentTimeMillis()
            val cooldownMillis = slowSeconds * 1000L

            if (now - lastMessageTime < cooldownMillis) {
                player.sendStyled(
                    plugin.config.getString("chat-slow-message")!!
                        .replace("{time}", slowSeconds.toString())
                )
                event.isCancelled = true
                return
            }
        }
    }

    private fun handleFiltering(event: AsyncChatEvent, rawMessage: String, player: Player) {
        Bukkit.getOnlinePlayers().filter { it.hasPermission(Permissions.CHAT_FILTER_NOTIFY_PERMISSION) }.forEach {
            it.sendStyled(
                plugin.config.getString("chat-message-filtered-staff-notification")!!
                    .replace("{sender}", miniMessage.serialize(player.displayName()))
                    .replace("{message}", rawMessage)
            )
        }

        val blockMessage = plugin.config.getBoolean("block-message-on-filter")
        if (blockMessage) {
            event.isCancelled = true
            if (plugin.config.getBoolean("notify-on-filter")) {
                player.sendStyled(plugin.config.getString("chat-filter-filtered")!!)
            }
        } else {
            val matches = filter.findFilteredWords(rawMessage)
            val censored = censorMessage(rawMessage, matches)
            event.message(style(censored))
        }

        plugin.config.getString("embed-body-chat-filter")?.let {
            it.replace("{sender}", player.name)
             .replace("{message}",  rawMessage)

            plugin.webhookManager.sendEmbedMessage(it, WebhookManager.NotificationReason.FILTER)
        }

    }

    private fun censorMessage(original: String, matches: List<WordMatch>): String {
        val result = StringBuilder(original)
        for (match in matches) {
            for (i in match.start until match.end) {
                if (result[i].isLetter()) {
                    result.setCharAt(i, '*')
                }
            }
        }
        return result.toString()
    }
}