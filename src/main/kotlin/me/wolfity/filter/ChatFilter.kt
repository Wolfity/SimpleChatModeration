package me.wolfity.filter

data class WordMatch(val start: Int, val end: Int)

class ChatFilter(filteredWords: List<String>) {

    private val root = TrieNode()

    val substitutions = mapOf(
        '1' to 'i',
        '!' to 'i',
        '@' to 'a',
        '3' to 'e',
        '0' to 'o',
        '$' to 's',
        '7' to 't'
    )

    init {
        for (word in filteredWords) {
            addWord(word.lowercase().filter { it.isLetter() })
        }
    }

    private fun addWord(word: String) {
        var node = root
        for (char in word) {
            node = node.children.computeIfAbsent(char) { TrieNode() }
        }
        node.isEnd = true
    }

    private fun normalizeInput(input: String): String {
        return input.lowercase().mapNotNull { c ->
            when {
                c.isLetter() -> c
                substitutions.containsKey(c) -> substitutions[c]
                else -> null
            }
        }.joinToString("")
    }

    fun containsFilteredWord(rawInput: String): Boolean {
        val normalized = normalizeInput(rawInput)
        var i = 0
        while (i < normalized.length) {
            var node = root
            var j = i
            while (j < normalized.length && node.children.containsKey(normalized[j])) {
                node = node.children[normalized[j]]!!
                if (node.isEnd) return true
                j++
            }
            i++
        }
        return false
    }

    /**
     * Finds all filtered words in a string.
     */
    fun findFilteredWords(rawInput: String): List<WordMatch> {
        val normalized = normalizeInput(rawInput)
        val matches = mutableListOf<WordMatch>()
        var i = 0
        while (i < normalized.length) {
            var node = root
            var j = i
            while (j < normalized.length && node.children.containsKey(normalized[j])) {
                node = node.children[normalized[j]]!!
                if (node.isEnd) {
                    matches.add(WordMatch(i, j + 1))
                }
                j++
            }
            i++
        }
        return matches
    }

    private class TrieNode(
        val children: MutableMap<Char, TrieNode> = mutableMapOf(),
        var isEnd: Boolean = false
    )
}