package me.wolfity.reports.gui

import me.wolfity.gui.GUI
import me.wolfity.gui.PaginatedGUI
import me.wolfity.plugin
import me.wolfity.reports.ChatReport
import me.wolfity.util.buildItem
import me.wolfity.util.formatTime
import me.wolfity.util.launchAsync
import me.wolfity.util.style
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ReportsGUI(
    guiOwner: Player,
    name: Component,
    items: List<ChatReport>
) : PaginatedGUI<ChatReport>(54, name, guiOwner, currentPage = 1, spaces = 28, items) {

    init {
        constructGUI()
        openGUI()
    }

    private fun constructGUI() {
        val borderItem =
            buildItem(Material.valueOf(plugin.config.getString("gui-border-item") ?: "GRAY_STAINED_GLASS_PANE"))
        val borderSlots = listOf(
            // Top row
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            // Left column
            9, 18, 27, 36,
            // Right column
            17, 26, 35, 44,
            // Bottom row
            46, 47, 48, 49, 50, 51, 52
        )

        borderSlots.forEach {
            setItem(it, borderItem)
        }

        if (hasPreviousPage()) {
            setItem(45, getPageLeftItem()) {
                decreaseCurrentPage()
                constructGUI()
            }
        } else {
            setItem(45, borderItem)
        }

        if (hasNextPage()) {
            setItem(53, getPageRightItem()) {
                increaseCurrentPage()
                constructGUI()
            }
        } else {
            setItem(53, borderItem)
        }

        val reportSlots = (0 until 54).filter { it !in borderSlots }

        val pageItems = getPageItems(currentPage)
        launchAsync {
            pageItems.forEachIndexed { index, report ->
                if (index < reportSlots.size) {
                    val slot = reportSlots[index]
                    val reportIconAction = getReportIconWithAction(report)
                    setItem(slot, reportIconAction.first) {
                        reportIconAction.second()
                    }
                }
            }
        }
    }

    private suspend fun getReportIconWithAction(chatReport: ChatReport): Pair<ItemStack, () -> Unit> {
        val reporterData = plugin.playerManager.getDataByUUID(chatReport.reporter)
        val reportedData = plugin.playerManager.getDataByUUID(chatReport.reported)
        val reporter = reporterData?.name ?: "Error (reporter)"
        val reported = reportedData?.name ?: "Error (reported)"

        val timestampText = formatTime(chatReport.timeStamp)
        val itemName = style(
            plugin.config.getString("gui-report-icon-title")!!
                .replace("{reporter}", chatReport.id.toString())
                .replace("{reportCount}", chatReport.id.toString())
                .replace("{reported}", reported)
        )

        val lore = plugin.config.getStringList("gui-report-icon-lore").map {
            it.replace("{reporter}", reporter)
                .replace("{reported}", reported)
                .replace("{time}", timestampText)
                .replace("{reason}", chatReport.reason)
        }.map { style(it) }

        val item = buildItem(Material.PLAYER_HEAD, itemName) {
            setLore(lore)
            setCustomTexture(reportedData?.skin)
        }

        return item to { ReportDetailGUI(player, style("<aqua>Action $reported's report"), chatReport, this) }

    }

}