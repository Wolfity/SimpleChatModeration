package me.wolfity.listeners

import me.wolfity.manager.PlayerManager
import me.wolfity.misc.UpdateChecker
import me.wolfity.plugin
import me.wolfity.util.launchAsync
import me.wolfity.util.sendStyled
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerListeners(private val playerManager: PlayerManager) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        launchAsync {
            val player = event.player
            if (playerManager.getDataByUUID(player.uniqueId) == null) {
                playerManager.registerPlayer(player.uniqueId, player.name)
            }
            if (player.isOp) {
                UpdateChecker().getVersion { version ->
                    if (plugin.description.version != version) {
                        val currentVersion = plugin.description.version
                        player.sendStyled("<red>There is an update available for Simple Chat Moderation: Current: $currentVersion, latest is $version")
                    }
                }
            }

        }
    }
}