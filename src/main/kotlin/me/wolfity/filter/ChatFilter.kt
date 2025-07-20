package me.wolfity.filter

data class WordMatch(val start: Int, val end: Int)

class ChatFilter(filteredWords: List<String>) {

    private val root = TrieNode()

    val substitutions = mapOf(
        '1' to 'i', '!' to 'i', '@' to 'a', '3' to 'e', '0' to 'o',
        '$' to 's', '7' to 't', '5' to 's', '+' to 't', '€' to 'e',
        '£' to 'l', '¥' to 'y', '§' to 's', '¿' to 'i', '¡' to 'i'
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

    private fun normalizeInput(rawInput: String): Pair<StringBuilder, List<Int>> {
        val normalized = StringBuilder()
        val indexMap = mutableListOf<Int>()
        for ((index, char) in rawInput.withIndex()) {
            val normChar = when {
                char.isLetter() -> char.lowercaseChar()
                substitutions.containsKey(char) -> substitutions[char]!!
                else -> null
            }
            if (normChar != null) {
                normalized.append(normChar)
                indexMap.add(index)
            }
        }
        return normalized to indexMap
    }

    fun containsFilteredWord(rawInput: String): Boolean {
        return matchFilteredWords(rawInput, firstOnly = true).isNotEmpty()
    }

    fun findFilteredWords(rawInput: String): List<WordMatch> {
        return matchFilteredWords(rawInput, firstOnly = false)
    }

    private fun matchFilteredWords(rawInput: String, firstOnly: Boolean): List<WordMatch> {
        val (normalized, indexMap) = normalizeInput(rawInput)
        val matches = mutableListOf<WordMatch>()

        var i = 0
        while (i < normalized.length) {
            var node = root
            var j = i
            while (j < normalized.length && node.children.containsKey(normalized[j])) {
                node = node.children[normalized[j]]!!
                if (node.isEnd) {
                    val startOriginal = indexMap[i]
                    val endOriginal = indexMap[j] + 1

                    // Word bound check
                    val before = if (startOriginal > 0) rawInput[startOriginal - 1] else ' '
                    val after = if (endOriginal < rawInput.length) rawInput[endOriginal] else ' '

                    if (!before.isLetterOrDigit() && !after.isLetterOrDigit()) {
                        matches.add(WordMatch(startOriginal, endOriginal))
                        if (firstOnly) return matches
                    }
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
