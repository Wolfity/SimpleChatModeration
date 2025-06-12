package me.wolfity.misc

import me.wolfity.SimpleChatMod.Companion.RESOURCE_ID
import me.wolfity.plugin
import org.bukkit.Bukkit
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.function.Consumer

class UpdateChecker {

    fun getVersion(consumer: Consumer<String?>) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            try {
                URL("https://api.spigotmc.org/legacy/update.php?resource=$RESOURCE_ID/~").openStream()
                    .use { `is` ->
                        Scanner(`is`).use { scan ->
                            if (scan.hasNext()) {
                                consumer.accept(scan.next())
                            }
                        }
                    }
            } catch (e: IOException) {
                plugin.logger.info("Unable to check for updates: " + e.message)
            }
        })
    }

}