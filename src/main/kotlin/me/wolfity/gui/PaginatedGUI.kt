package me.wolfity.gui

import me.wolfity.plugin
import me.wolfity.util.ItemBuilder
import me.wolfity.util.style

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.math.ceil

abstract class PaginatedGUI<T>(
    size: Int,
    name: Component,
    owner: Player,
    internal var currentPage: Int = 1,
    private val spaces: Int,
    private val allItems: List<T>
) : GUI(size, name, owner) {

    fun getCurrentPage(): Int = currentPage

    /**
     * Returns the items that should be displayed on a given page.
     *
     * @param page The page we want to see.
     * @return The list of items to display.
     */
    protected fun getPageItems(page: Int): List<T> {
        val upper = page * spaces
        val lower = upper - spaces
        return allItems.subList(lower.coerceAtLeast(0), upper.coerceAtMost(allItems.size))
    }

    protected fun getMaxPages(): Int {
        if (spaces <= 0) return 1
        return ceil(allItems.size.toDouble() / spaces).toInt()
    }

    protected fun getPageItems(): List<T> = getPageItems(currentPage)

    protected fun isPageValid(items: List<T>, page: Int): Boolean {
        if (page <= 0) return false
        val lower = (page * spaces) - spaces
        return lower < items.size
    }

    protected fun increaseCurrentPage() {
        currentPage++
        safelyClear()
        openGUI()
    }

    protected fun decreaseCurrentPage() {
        currentPage--
        safelyClear()
        openGUI()
    }

    // Safely clear the GUI, so we don't potentially have stale click handlers.
    private fun safelyClear() {
        for (i in 0 until inventory.size) {
            setItem(i, null)
        }
    }

    protected fun getPageRightItem(): ItemStack {
        return ItemBuilder(
            Material.ARROW,
            style(
                plugin.config.getString("gui-next-page-text")!!
                    .replace("{current}", currentPage.toString())
                    .replace("{max}", getMaxPages().toString())
            )
        ).build()
    }

    fun hasNextPage(): Boolean {
        return currentPage < getMaxPages()
    }

    fun hasPreviousPage(): Boolean {
        return currentPage > 1
    }

    protected fun getPageLeftItem(): ItemStack {
        return ItemBuilder(
            Material.ARROW,
            style(
                plugin.config.getString("gui-previous-page-text")!!
                    .replace("{current}", currentPage.toString())
                    .replace("{max}", getMaxPages().toString())
            )
        ).build()
    }


}
