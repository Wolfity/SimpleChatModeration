package me.wolfity.gui

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent

class GUIListener : Listener {

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        val guiUUID = GUI.openInventories[player.uniqueId] ?: return
        val gui = GUI.inventoriesByUUID[guiUUID] ?: return

        // Prevent interaction while GUI is open
        event.isCancelled = true
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val clickedInventory = event.clickedInventory ?: return

        val guiUUID = GUI.openInventories[player.uniqueId] ?: return
        val gui = GUI.inventoriesByUUID[guiUUID] ?: return

        if (clickedInventory != gui.inventory) {
            event.isCancelled = true
            return
        }

        val action = gui.actions[event.slot]
        event.isCancelled = true
        action?.invoke(event)
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val guiId = GUI.openInventories[player.uniqueId] ?: return
        val gui = GUI.inventoriesByUUID[guiId] ?: return
        GUI.openInventories.remove(guiId)
        GUI.inventoriesByUUID.remove(player.uniqueId)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        val guiId = GUI.openInventories[player.uniqueId] ?: return
        val gui = GUI.inventoriesByUUID[guiId] ?: return
        gui.close()
    }


}