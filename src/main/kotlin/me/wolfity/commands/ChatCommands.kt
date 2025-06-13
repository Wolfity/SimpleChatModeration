package me.wolfity.commands

import me.wolfity.SimpleChatMod
import me.wolfity.constants.Permissions
import me.wolfity.reports.ChatReport
import me.wolfity.reports.gui.ReportsGUI
import me.wolfity.util.formatTime
import me.wolfity.util.launchAsync
import me.wolfity.util.sendStyled
import me.wolfity.util.style
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Optional
import revxrsal.commands.bukkit.annotation.CommandPermission
import java.security.Permission

class ChatCommands(val plugin: SimpleChatMod) {

    @Command("mutechat")
    @CommandPermission(Permissions.MUTE_CHAT_PERMISSION)
    fun onMuteChat(sender: Player) {
        plugin.chatStateManager.toggleChatMuted(sender)
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

    @Command("chatreports", "handlechatreports", "reports")
    @CommandPermission(Permissions.CHAT_REPORT_HANDLE_PERMISSION)
    fun onHandleReport(sender: Player, @Optional @Named("player") target: UserCommandParameter?) {
        launchAsync {
            val reports = if (target == null) {
                plugin.chatReportManager.getAllReports()
            } else {
                val targetUser = plugin.playerManager.getDataByName(target.name)
                if (targetUser == null) {
                    sender.sendStyled("<red>This user does not exist!")
                    return@launchAsync
                }
                plugin.chatReportManager.getReportsAgainstPlayer(targetUser.uuid)
            }

            val title = if (target != null) {
                "${target.name}'s Chat Reports"
            } else "Chat Reports"
            ReportsGUI(sender, style("<red>$title"), reports)
        }
    }

    @Command("chatreport")
    fun onChatReport(
        sender: Player,
        @Named("target") target: UserCommandParameter,
        @Named("reason") reason: List<String>
    ) {
        launchAsync {
            val targetUser = plugin.playerManager.getDataByName(target.name)
            if (targetUser == null) {
                sender.sendStyled("<red>This player does not exist!")
                return@launchAsync
            }

            if (targetUser.uuid == sender.uniqueId) {
                sender.sendStyled("<red>You cannot report yourself!")
                return@launchAsync
            }

            val hasPreviousMessages = plugin.chatMessageManager.hasMessagesBeforeTimestamp(targetUser.uuid, System. currentTimeMillis())
            if (!hasPreviousMessages) {
                sender.sendStyled(plugin.config.getString("no-messages-in-past-time")!!
                    .replace("{minutes}", plugin.config.getInt("chat-report-generation-timespan-minutes").toString()))
                return@launchAsync
            }

            val hasOutstandingReports = plugin.chatReportManager.hasReportFromTo(sender.uniqueId, targetUser.uuid)
            if (hasOutstandingReports) {
                sender.sendStyled("<red>You already made a report in the last hour against this player!")
                return@launchAsync
            }

            plugin.chatReportManager.createReport(sender.uniqueId, targetUser.uuid, reason.joinToString(" "))

            val staffNotification = plugin.config.getString("chat-report-notification")!!
                .replace("{reporter}", sender.name)
                .replace("{reported}", targetUser.name)
                .replace("{reason}", reason.joinToString(" "))

            Bukkit.getOnlinePlayers()
                .filter { it.hasPermission(Permissions.CHAT_REPORT_HANDLE_PERMISSION) }
                .forEach { player ->
                    player.sendStyled(staffNotification)
                }

            sender.sendStyled(plugin.config.getString("chat-report-success")!!)

        }

    }

}