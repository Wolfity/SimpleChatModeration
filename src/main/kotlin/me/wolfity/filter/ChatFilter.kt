package me.wolfity.filter

data class WordMatch(val start: Int, val end: Int)

class ChatFilter(filteredWords: List<String>) {

    // regex detects filtered words even when their letters are separated
    // by spaces, symbols, or non-alphanumeric characters, like f.u c_k.
    private val patterns: List<Regex> = filteredWords.map {
        val spacedPattern = it.lowercase().toCharArray()
            .joinToString("""[\s\W_]*""") { Regex.escape(it.toString()) }

        Regex(spacedPattern, RegexOption.IGNORE_CASE)
    }

    fun containsFilteredWord(input: String): Boolean {
        val strippedInput = input.lowercase().replace(Regex("<.*?>"), "")
        return patterns.any { it.containsMatchIn(strippedInput) }
    }

    /**
     * Finds all filtered words in a string.
     */
    fun findFilteredWords(input: String): List<WordMatch> {
        val matches = mutableListOf<WordMatch>()
        val cleanedInput = input.lowercase()
        for (regex in patterns) {
            regex.findAll(cleanedInput).forEach { match ->
                matches.add(WordMatch(match.range.first, match.range.last + 1))
            }
        }
        return matches
    }
}
