package me.wolfity.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import me.wolfity.SimpleChatMod
import me.wolfity.cache.ChatMessageCache
import me.wolfity.constants.Permissions
import me.wolfity.events.ChatMutedEvent
import me.wolfity.filter.ChatFilter
import me.wolfity.filter.WordMatch
import me.wolfity.logging.ChatMessage
import me.wolfity.sql.ChatMessages
import me.wolfity.util.miniMessage
import me.wolfity.util.sendStyled
import me.wolfity.util.style
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
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
    fun onChat(event: AsyncChatEvent) {
        val player = event.player
        val isChatMuted = plugin.chatManager.chatMuted
        val message = event.message()

        if (isChatMuted && !player.hasPermission(Permissions.MUTE_CHAT_BYPASS_PERMISSION)) {
            event.isCancelled = true
            player.sendStyled(plugin.config.getString("chat-muted-message")!!)
            return
        }

        val rawMessage = miniMessage.serialize(message)
        val containsFiltered = filter.containsFilteredWord(rawMessage)

        val pureContent = PlainTextComponentSerializer.plainText().serialize(message)

        ChatMessageCache.addMessage(ChatMessage(sender = player.uniqueId, content = pureContent))

        if (containsFiltered && !player.hasPermission(Permissions.CHAT_FILTER_BYPASS_PERMISSION)) {
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