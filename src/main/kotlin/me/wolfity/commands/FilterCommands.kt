package me.wolfity.commands

import me.wolfity.constants.Permissions
import me.wolfity.plugin
import me.wolfity.util.sendStyled
import me.wolfity.util.style
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.CommandPlaceholder
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.annotation.SuggestWith
import revxrsal.commands.annotation.Usage
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("filter", "chatfilter")
@CommandPermission(Permissions.ADMIN_PERMISSION)
class FilterCommands {

    @CommandPlaceholder
    fun onPlaceholder(sender: Player) {
        sender.sendStyled("<red>Invalid Usage: /filter <add|remove|list>")
    }

    @Subcommand("add")
    @Usage("/filter add <word>")
    fun addFilterWord(sender: Player, @Named("word") word: String) {
        val currentList = plugin.filteredWordsConfig.getStringList("filtered-words").toMutableList()

        if (currentList.contains(word.lowercase())) {
            sender.sendStyled(plugin.config.getString("word-already-in-filter")!!.replace("{word}", word))
            return
        }

        currentList.add(word.lowercase())
        plugin.filteredWordsConfig.set("filtered-words", currentList)
        plugin.chatFilter.addWord(word.lowercase())
        sender.sendStyled("<green>Added '<gray>$word<green>' to filtered words.")
    }

    @Subcommand("remove")
    @Usage("/filter remove <word>")
    fun removeFilterWord(sender: Player, @Named("word") @SuggestWith(WordSuggestions::class) word: String) {
        val currentList = plugin.filteredWordsConfig.getStringList("filtered-words").toMutableList()

        if (!currentList.remove(word)) {
            sender.sendStyled(plugin.config.getString("word-not-in-filter")!!.replace("{word}", word))
            return
        }

        plugin.filteredWordsConfig.set("filtered-words", currentList)
        plugin.chatFilter.removeWord(word.lowercase())
        sender.sendStyled("<green>Removed '<gray>$word<green>' from filtered words.")
    }

    @Subcommand("list")
    @Usage("/filter list <page>")
    fun onFilterList(sender: Player, @Named("page") @Optional page: Int?) {
        val words = plugin.filteredWordsConfig.getStringList("filtered-words")
        val filterPageSize = plugin.config.getInt("filtered-word-list.page-size")
        if (words.isEmpty()) {
            sender.sendStyled(plugin.config.getString("no-filtered-words")!!)
            return
        }

        val totalPages = calculateTotalPages(filterPageSize, words.size)
        val pageNum = (page ?: 1).coerceIn(1, totalPages)

        sendFilterListHeader(sender, pageNum, totalPages)

        val startIndex = (pageNum - 1) * filterPageSize
        val endIndex = (startIndex + filterPageSize).coerceAtMost(words.size)

        for (i in startIndex until endIndex) {
            val word = words[i]
            sendFilterListEntry(sender, word)
        }

        sendFilterListFooter(sender, pageNum, totalPages)
    }

    private fun calculateTotalPages(pageSize: Int, listSize: Int): Int {
        return (listSize + pageSize - 1) / pageSize
    }

    private fun sendFilterListHeader(player: Player, pageNum: Int, totalPages: Int) {
        val header = plugin.config.getString("filtered-word-list.header")!!
            .replace("{page}", pageNum.toString())
            .replace("{maxPages}", totalPages.toString())
        player.sendStyled(header)
    }

    private fun sendFilterListEntry(player: Player, word: String) {
        val lineTemplate = plugin.config.getString("filtered-word-list.entry")!!
        val baseLine = lineTemplate.replace("{word}", word)

        val removeText = plugin.config.getString("filtered-word-list.remove-button")!!
        val removeComponent = style(removeText)
            .clickEvent(ClickEvent.runCommand("/filter remove $word"))
            .hoverEvent(
                HoverEvent.showText(
                    style(
                        plugin.config.getString("filtered-word-list.remove-word-hover-text")!!
                            .replace("{word}", word)
                    )
                )
            )

        player.sendMessage(
            style(baseLine).append(Component.space()).append(removeComponent)
        )
    }

    private fun sendFilterListFooter(player: Player, pageNum: Int, totalPages: Int) {
        fun createNavComponent(
            text: String,
            command: String?,
            hoverText: String
        ): Component {
            var component = style(text)
            if (command != null) {
                component = component
                    .clickEvent(ClickEvent.runCommand(command))
            }

            return component.hoverEvent(HoverEvent.showText(style(hoverText)))
        }

        val previousPage = pageNum - 1
        val hoverPrevText = plugin.config.getString("filtered-word-list.previous-page-hover-text")!!
            .replace("{previousPage}", previousPage.toString())

        val prevComponent = if (pageNum > 1) {
            createNavComponent(
                plugin.config.getString("filtered-word-list.previous-button")!!,
                "/filter list $previousPage",
                hoverPrevText
            )
        } else {
            createNavComponent(
                plugin.config.getString("filtered-word-list.previous-button")!!,
                null,
                hoverPrevText
            )
        }

        val nextPage = pageNum + 1
        val hoverNextPage = plugin.config.getString("filtered-word-list.next-page-hover-text")!!
            .replace("{nextPage}", nextPage.toString())
        val nextComponent = if (pageNum < totalPages) {
            createNavComponent(
                plugin.config.getString("filtered-word-list.next-button")!!,
                "/filter list $nextPage",
                hoverNextPage
            )
        } else {
            createNavComponent(
                plugin.config.getString("filtered-word-list.next-button")!!,
                null,
                hoverNextPage
            )
        }

        val footerComponent = style(plugin.config.getString("filtered-word-list.footer")!!)
            .replaceText { it.matchLiteral("{nextButton}").replacement(nextComponent) }
            .replaceText { it.matchLiteral("{previousButton}").replacement(prevComponent) }

        player.sendMessage(footerComponent)
    }


}