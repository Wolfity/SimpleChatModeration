package me.wolfity.misc

import me.wolfity.SimpleChatMod.Companion.RESOURCE_ID
import me.wolfity.plugin
import org.bukkit.Bukkit
import java.io.IOException
import java.net.URL
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.Consumer

object UpdateChecker {

    fun getVersion(consumer: Consumer<String?>) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            try {
                val jsonString = URL("https://api.spigotmc.org/simple/0.2/index.php?action=getResource&id=$RESOURCE_ID")
                    .readText()

                val regex = """"current_version"\s*:\s*"([^"]+)"""".toRegex()
                val matchResult = regex.find(jsonString)
                val currentVersion = matchResult?.groups?.get(1)?.value

                consumer.accept(currentVersion)
            } catch (e: IOException) {
                plugin.logger.info("Unable to check for updates: " + e.message)
                consumer.accept(null)
            }
        })
    }


}