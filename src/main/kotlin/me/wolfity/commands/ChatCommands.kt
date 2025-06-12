package me.wolfity.commands

import me.wolfity.SimpleChatMod
import me.wolfity.constants.Permissions
import me.wolfity.util.formatTime
import me.wolfity.util.sendStyled
import me.wolfity.util.style
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Optional
import revxrsal.commands.bukkit.annotation.CommandPermission

class ChatCommands(val plugin: SimpleChatMod) {

    @Command("mutechat")
    @CommandPermission(Permissions.MUTE_CHAT_PERMISSION)
    fun onMuteChat(sender: Player) {
        plugin.chatManager.toggleChatMuted(sender)
    }


    @Command("chatlogs")
    @CommandPermission(Permissions.VIEW_CHAT_LOG_PERMISSION)
    suspend fun chatLogs(
        sender: Player,
        @Named("target") target: UserCommandParameter,
        @Named("page") @Optional page: Int?
    ) {
        val pageNum = page?.coerceAtLeast(1) ?: 1

        val targetPlayerData = plugin.playerManager.getDataByName(target.name)
        if (targetPlayerData == null) {
            sender.sendStyled("<red>No user by the name of ${target.name} exists!")
            return
        }

        val maxPages = plugin.chatMessageManager.getMaxPagesForPlayer(targetPlayerData.uuid)
        if (pageNum > maxPages) {
            sender.sendStyled("<red>This player has no logs on that page!")
            return
        }

        val messages = plugin.chatMessageManager.getMessagesFrom(targetPlayerData.uuid, pageNum)

        if (messages.isEmpty()) {
            sender.sendStyled(plugin.config.getString("no-logs-found")!!.replace("{player}", target.name))
            return
        }

        sender.sendStyled(
            plugin.config.getString("chat-log-header")!!
                .replace("{player}", targetPlayerData.name)
                .replace("{pageNum}", pageNum.toString())
                .replace("{maxPages}", maxPages.toString())
        )

        val entryMessage = plugin.config.getString("chat-log-entry")!!

        messages.forEach { msg ->
            sender.sendStyled(
                entryMessage
                    .replace("{time}", formatTime(msg.timestamp))
                    .replace("{message}", PlainTextComponentSerializer.plainText().serialize(style(msg.content)))
            )
        }

        sender.sendStyled(plugin.config.getString("chat-log-footer")!!)
    }

}