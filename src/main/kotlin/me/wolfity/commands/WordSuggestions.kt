package me.wolfity.commands

import me.wolfity.plugin
import revxrsal.commands.autocomplete.SuggestionProvider
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.node.ExecutionContext

class WordSuggestions : SuggestionProvider<BukkitCommandActor> {
    override fun getSuggestions(p0: ExecutionContext<BukkitCommandActor?>): Collection<String?> {
        return plugin.filteredWordsConfig.getStringList("filtered-words")
    }
}