package me.wolfity.webhook

import me.wolfity.plugin
import me.wolfity.util.launchAsync
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class WebhookManager {

    private val webhookUrl: String by lazy {
        plugin.config.getString("webhook-url")!!
    }

    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(10.seconds.toJavaDuration())
        .build()

    fun sendEmbedMessage(message: String, reason: NotificationReason) {
        val cleanMessage = stripMiniMessage(message)
        val jsonPayload = buildEmbedJson(cleanMessage, reason)
        launchAsync {
            try {
                val request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build()

                httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .thenAccept { response ->
                        if (response.statusCode() !in 200..299) {
                            plugin.logger.warning("Failed to send webhook. HTTP ${response.statusCode()}")
                        }
                    }
            } catch (e: Exception) {
                plugin.logger.warning("Error sending webhook: ${e.message}")
            }
        }

    }

    private fun stripMiniMessage(input: String): String {
        return input.replace(Regex("<.*?>"), "")
    }

    private fun buildEmbedJson(message: String, reason: NotificationReason): String {
        val title =
            plugin.config.getString(if (reason == NotificationReason.CHAT_REPORT) "embed-title-chat-report" else "embed-title-chat-filter")!!
        val color =
            plugin.config.getString(if (reason == NotificationReason.CHAT_REPORT) "embed-color-chat-report" else "embed-color-chat-filter")!!
        val footer =
            plugin.config.getString(if (reason == NotificationReason.CHAT_REPORT) "embed-footer-chat-report" else "embed-footer-chat-filter")!!

        val colorInt = parseHexColor(color)

        return """
            {
              "embeds": [{
                "title": "${escapeJson(title)}",
                "description": "${escapeJson(message)}",
                "color": $colorInt,
                "footer": {"text": "${escapeJson(footer)}"}
              }]
            }
        """.trimIndent()
    }

    private fun escapeJson(input: String): String {
        return input.replace("\"", "\\\"")
    }

    private fun parseHexColor(hex: String): Int {
        return hex.trim()
            .replace("#", "")
            .toInt(16)
    }

    enum class NotificationReason {
        CHAT_REPORT,
        FILTER
    }

}