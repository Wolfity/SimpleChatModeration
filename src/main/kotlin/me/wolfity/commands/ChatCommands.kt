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
import revxrsal.commands.annotation.Range
import revxrsal.commands.bukkit.annotation.CommandPermission
import java.security.Permission

class ChatCommands(val plugin: SimpleChatMod) {

    @Command("mutechat")
    @CommandPermission(Permissions.MUTE_CHAT_PERMISSION)
    fun onMuteChat(sender: Player) {
        plugin.chatStateManager.toggleChatMuted(sender)
    }

    @Command("slowchat")
    @CommandPermission(Permissions.SLOW_CHAT)
    fun onSlowChat(sender: Player, @Named("seconds") @Range(min = 0.0) amount: Int) {
        plugin.chatStateManager.slowChat(amount)
    }

    @Command("resetslowchat")
    @CommandPermission(Permissions.SLOW_CHAT)
    fun onSlowChat(sender: Player) {
        plugin.chatStateManager.resetSlow()
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
            sender.sendStyled(plugin.config.getString("player-does-not-exist")!!.replace("{player}", target.name))
            return
        }

        val maxPages = plugin.chatMessageManager.getMaxPagesForPlayer(targetPlayerData.uuid)
        if (pageNum > maxPages) {
            sender.sendStyled(plugin.config.getString("invalid-chatlog-page")!!)
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
                    sender.sendStyled(plugin.config.getString("player-does-not-exist")!!.replace("{player}", target.name))
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
                sender.sendStyled(plugin.config.getString("player-does-not-exist")!!.replace("{player}", target.name))
                return@launchAsync
            }

            if (targetUser.uuid == sender.uniqueId) {
                sender.sendStyled(plugin.config.getString("cannot-self-report")!!)
                return@launchAsync
            }

            val hasPreviousMessages =
                plugin.chatMessageManager.hasMessagesBeforeTimestamp(targetUser.uuid, System.currentTimeMillis())
            if (!hasPreviousMessages) {
                sender.sendStyled(
                    plugin.config.getString("no-messages-in-past-time")!!
                        .replace(
                            "{minutes}",
                            plugin.config.getInt("chat-report-generation-timespan-minutes").toString()
                        )
                )
                return@launchAsync
            }

            val maxReportTimeFrameSamePerson = plugin.config.getLong("max-same-person-report-time-minutes")
            val hasOutstandingReports =
                plugin.chatReportManager.hasReportFromTo(sender.uniqueId, targetUser.uuid, maxReportTimeFrameSamePerson)
            if (hasOutstandingReports) {
                sender.sendStyled(
                    plugin.config.getString("already-reported-within-timeframe")!!.replace(
                        "{time}", maxReportTimeFrameSamePerson.toInt().toString()
                    )
                )
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