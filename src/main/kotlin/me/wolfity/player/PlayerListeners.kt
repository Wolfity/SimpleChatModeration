package me.wolfity.player

import me.wolfity.misc.UpdateChecker
import me.wolfity.plugin
import me.wolfity.util.launchAsync
import me.wolfity.util.sendStyled
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerListeners(private val playerManager: PlayerManager) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        launchAsync {
            val player = event.player
            val storedData = playerManager.getDataByUUID(player.uniqueId)
            val extractedSkinTexture = extractSkinTexture(player)

            if (storedData == null) {
                playerManager.registerPlayer(player.uniqueId, player.name, extractedSkinTexture)
            } else {
                if (storedData.skin != extractedSkinTexture) {
                    playerManager.updatePlayerSkin(player.uniqueId, extractedSkinTexture)
                }
            }


            if (player.isOp) {
                UpdateChecker.getVersion { version ->
                    if (plugin.description.version != version) {
                        val currentVersion = plugin.description.version
                        val msg = plugin.config.getString("outdated-version")!!
                            .replace("{currentVersion}", currentVersion)
                            .replace("{newVersion}", version!!)
                        player.sendStyled(msg)
                    }
                }
            }
        }
    }

    private fun extractSkinTexture(player: Player): String? {
        val texture = player.playerProfile.properties.firstOrNull { it.name.equals("textures") }
        return texture?.value
    }

}