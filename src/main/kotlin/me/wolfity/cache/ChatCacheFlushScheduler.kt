package me.wolfity.cache

import me.wolfity.logging.ChatMessageManager
import me.wolfity.plugin
import me.wolfity.util.launchAsync
import org.bukkit.scheduler.BukkitRunnable

class ChatCacheFlushScheduler(private val manager: ChatMessageManager) {

    companion object {
        const val DRAIN_INTERVAL_TICKS: Long = 60 * 20
    }

    private var task: BukkitRunnable? = null

    fun start() {
        plugin.logger.info("[SimpleChatModeration] - Cache has been initialised")
        task = object : BukkitRunnable() {
            override fun run() {
                val messagesToFlush = ChatMessageCache.drain()
                if (messagesToFlush.isNotEmpty()) {
                    launchAsync {
                        manager.saveChatMessage(messagesToFlush)
                        plugin.logger.info("[SimpleChatModeration] - Cache has been flushed to the database")
                    }
                }
            }
        }
        task!!.runTaskTimer(plugin, DRAIN_INTERVAL_TICKS, DRAIN_INTERVAL_TICKS)
    }

    fun shutdown() {
        task?.cancel()
        val remaining = ChatMessageCache.drain()
        if (remaining.isEmpty()) return
        launchAsync {
            manager.saveChatMessage(remaining)
        }
    }
}