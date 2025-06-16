package me.wolfity.cache

import me.wolfity.logging.ChatMessage
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ChatMessageCache {

    private val playerMessages = ConcurrentHashMap<UUID, MutableList<ChatMessage>>()

    fun addMessage(message: ChatMessage) {
        playerMessages.computeIfAbsent(message.sender) { mutableListOf() }.add(message)
    }

    fun getMessages(uuid: UUID): List<ChatMessage>? {
        return playerMessages[uuid]?.toList()
    }

    fun drain(): List<ChatMessage> {
        val all = mutableListOf<ChatMessage>()
        val iterator = playerMessages.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            all.addAll(entry.value)
            iterator.remove()
        }
        return all
    }
}
