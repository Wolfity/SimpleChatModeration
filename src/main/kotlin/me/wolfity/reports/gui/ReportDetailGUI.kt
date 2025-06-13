package me.wolfity.reports.gui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.wolfity.gui.GUI
import me.wolfity.logging.ChatMessage
import me.wolfity.plugin
import me.wolfity.reports.ChatReport
import me.wolfity.util.buildItem
import me.wolfity.util.formatTime
import me.wolfity.util.launchAsync
import me.wolfity.util.sendStyled
import me.wolfity.util.style
import net.kyori.adventure.text.Component
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.UUID

class ReportDetailGUI(
    player: Player,
    name: Component,
    private val chatReport: ChatReport,
    private val previousGUI: GUI
) : GUI(9, name, player) {

    init {
        constructGUI()
        openGUI()
    }

    private fun constructGUI() {
        setItem(2, buildItem(Material.DIAMOND_SWORD, style("<aqua>Generate chat logs"))) {
            close()
            player.sendStyled("<green>Generating the report!")
            val timestamp = chatReport.timeStamp
            val reporter = chatReport.reporter
            val reported = chatReport.reported
            launchAsync {
                val reporterName = plugin.playerManager.getDataByUUID(reporter)?.name ?: "Invalid Reporter"
                val reportedName = plugin.playerManager.getDataByUUID(reported)?.name ?: "Invalid Reported"
                val reporterMessages = plugin.chatMessageManager.generateReportLog(reporter, timestamp)
                val reportedMessages = plugin.chatMessageManager.generateReportLog(reported, timestamp)

                val allMessages = (reporterMessages + reportedMessages).sortedBy { it.timestamp }

                val uuidToName = mapOf(
                    reporter to reporterName,
                    reported to reportedName
                )

                val reportText = buildReportText(chatReport, reporterName, reportedName, allMessages, uuidToName)

                val url = uploadToDpaste(reportText)
                if (url != null) {
                    player.sendStyled("<green>Chat report uploaded: <click:open_url:$url><underlined>$url</underlined></click>")
                } else {
                    player.sendStyled("<red>Failed to upload chat report.")
                }
            }
        }

        setItem(4, buildItem(Material.RED_WOOL, style("<red>Close report"))) {
            launchAsync {
                plugin.chatReportManager.resolveReport(chatReport.id)
                player.sendStyled("<red>Closed report ${chatReport.id}")
                close()
            }
        }

        setItem(6, buildItem(Material.BEDROCK, style("<dark_red>Close all reports from this player")) {
            setLore(
                listOf(
                    style("<red><bold>Careful!"),
                    style("This action closes all open reports against this player!")
                )
            )
        }) {
            launchAsync {
                val closed = plugin.chatReportManager.resolveAllAgainst(chatReport.reported)
                val target = plugin.playerManager.getDataByUUID(chatReport.reported)?.name ?: "Error"
                player.sendStyled("<green>Successfully closed a total of $closed against <dark_green>$target")
                close()
            }
        }

        setItem(8, buildItem(Material.BARRIER, style("<red>Back"))) {
            previousGUI.openGUI()
        }

    }

    private suspend fun uploadToDpaste(content: String): String? = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val requestBody = FormBody.Builder()
                .add("content", content)
                .add("syntax", "text")
                .add("expiry_days", "3   ")
                .build()

            val request = Request.Builder()
                .url("https://dpaste.org/api/")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val url = response.body?.string()
                if (response.isSuccessful && url != null) url.trim() else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



    private fun formatChatMessage(senderName: String, msg: ChatMessage): String {
        return "[${formatTime(msg.timestamp, true)}] $senderName: ${msg.content}"
    }

    private fun buildReportText(
        chatReport: ChatReport,
        reporterName: String,
        reportedName: String,
        messages: List<ChatMessage>,
        uuidToName: Map<UUID, String>
    ): String {
        return buildString {
            appendLine("Chat Report ID: ${chatReport.id}")
            appendLine("Timestamp: ${chatReport.timeStamp}")
            appendLine("Reporter: $reporterName")
            appendLine("Reported: $reportedName")
            appendLine("")
            appendLine("=== Conversation ===")
            appendLine("")

            if (messages.isEmpty()) {
                appendLine("No messages found.")
            } else {
                messages.forEach { msg ->
                    val senderName = uuidToName[msg.sender] ?: "Unknown"
                    appendLine(formatChatMessage(senderName, msg))
                }
            }
        }
    }


}