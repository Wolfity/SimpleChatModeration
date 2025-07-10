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
            addWord(word)
        }
    }

    fun addWord(rawWord: String) {
        val word = rawWord.lowercase().filter { it.isLetter() }
        var node = root
        for (char in word) {
            node = node.children.computeIfAbsent(char) { TrieNode() }
        }
        node.isEnd = true
    }

    fun removeWord(rawWord: String) {
        val word = rawWord.lowercase().filter { it.isLetter() }
        removeWord(root, word, 0)
    }

    private fun removeWord(node: TrieNode, word: String, index: Int): Boolean {
        if (index == word.length) {
            if (!node.isEnd) return false
            node.isEnd = false
            return node.children.isEmpty()
        }

        val char = word[index]
        val child = node.children[char] ?: return false

        val shouldDeleteChild = removeWord(child, word, index + 1)

        if (shouldDeleteChild) {
            node.children.remove(char)
            return node.children.isEmpty() && !node.isEnd
        }

        return false
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
