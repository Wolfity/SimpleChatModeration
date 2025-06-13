package me.wolfity.gui

import me.wolfity.plugin
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID

abstract class GUI protected constructor(
    size: Int,
    name: Component,
    protected val player: Player
) {

    internal val inventory: Inventory = Bukkit.createInventory(player, size, name)
    private val uuid: UUID = UUID.randomUUID()
    internal val actions: MutableMap<Int, (InventoryClickEvent) -> Unit> = HashMap()

    init {
        inventoriesByUUID[uuid] = this
    }

    protected fun setItem(slot: Int, stack: ItemStack, action: ((InventoryClickEvent) -> Unit)? = null) {
        inventory.setItem(slot, stack)
        action?.let { actions[slot] = it }
    }

    protected fun setItem(slot: Int, item: ItemStack?) {
        inventory.setItem(slot, item)
        actions.remove(slot)
    }


    fun openGUI() {
        object : BukkitRunnable() {
            override fun run() {
                player.openInventory(inventory)
                openInventories[player.uniqueId] = uuid
            }
        }.runTask(plugin)

    }

    fun close() {
        object : BukkitRunnable() {
            override fun run() {
                player.closeInventory()
                openInventories.remove(player.uniqueId)
            }
        }.runTask(plugin)
    }

    fun getActions(): Map<Int, (InventoryClickEvent) -> Unit> = actions


    companion object {
        val inventoriesByUUID: MutableMap<UUID, GUI> = HashMap()
        val openInventories: MutableMap<UUID, UUID> = HashMap()
    }
}
