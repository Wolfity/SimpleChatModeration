package me.wolfity.logging

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.wolfity.cache.ChatMessageCache
import me.wolfity.plugin
import me.wolfity.db.ChatMessages
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class ChatMessageManager {

    companion object {
        const val CHAT_LOG_PAGE_SIZE: Int = 10
    }

    suspend fun saveChatMessage(messages: List<ChatMessage>) = withContext(Dispatchers.IO) {
        transaction {
            ChatMessages.batchInsert(messages) { message ->
                this[ChatMessages.sender] = message.sender
                this[ChatMessages.timestamp] = message.timestamp
                this[ChatMessages.content] = message.content
            }
        }
    }

    suspend fun hasMessagesBeforeTimestamp(player: UUID, centerTimestamp: Long): Boolean = withContext(Dispatchers.IO) {
        val bound = plugin.config.getInt("chat-report-generation-timespan-minutes")
        val fromTimestamp = centerTimestamp - bound * 60_000

        val cacheMessagesExist = ChatMessageCache.getMessages(player)
            ?.any { it.timestamp in fromTimestamp..<centerTimestamp } ?: false

        val dbMessagesExist = transaction {
            ChatMessages
                .select(ChatMessages.id)
                .where {
                    (ChatMessages.sender eq player) and
                            (ChatMessages.timestamp greaterEq fromTimestamp) and
                            (ChatMessages.timestamp less centerTimestamp)
                }
                .limit(1)
                .any()
        }

        cacheMessagesExist || dbMessagesExist
    }



    suspend fun generateReportLog(
        player: UUID,
        centerTimestamp: Long,
    ): List<ChatMessage> = withContext(Dispatchers.IO) {

        val bound = plugin.config.getInt("chat-report-generation-timespan-minutes")

        val fromTimestamp = centerTimestamp - bound * 60_000 // convert to ms
        val toTimestamp = centerTimestamp + bound * 60_000

        val cacheMessages = ChatMessageCache.getMessages(player)
            ?.filter { it.timestamp in fromTimestamp..toTimestamp }
            ?.sortedByDescending { it.timestamp } ?: emptyList()

        val dbMessages = transaction {
            ChatMessages.selectAll()
                .where {
                    (ChatMessages.sender eq player) and
                            (ChatMessages.timestamp.between(fromTimestamp, toTimestamp))
                }
                .orderBy(ChatMessages.timestamp to SortOrder.DESC)
                .map {
                    ChatMessage(
                        id = it[ChatMessages.id],
                        sender = it[ChatMessages.sender],
                        timestamp = it[ChatMessages.timestamp],
                        content = it[ChatMessages.content]
                    )
                }
        }

        mergeSortedLists(dbMessages, cacheMessages)
    }


    suspend fun getMessagesFrom(uuid: UUID, page: Int): List<ChatMessage> = withContext(Dispatchers.IO) {
        if (page <= 1) {
            getFirstPageMessages(uuid, CHAT_LOG_PAGE_SIZE)
        } else {
            getPagedMessages(uuid, page, CHAT_LOG_PAGE_SIZE)
        }
    }

    suspend fun getMaxPagesForPlayer(uuid: UUID): Int = withContext(Dispatchers.IO) {
        val cacheMessagesCount = ChatMessageCache.getMessages(uuid)?.size ?: 0

        val dbCount = transaction {
            ChatMessages
                .select(ChatMessages.id.count())
                .where { ChatMessages.sender eq uuid }
                .map { it[ChatMessages.id.count()] }
                .firstOrNull() ?: 0L
        }

        val totalMessages = cacheMessagesCount + dbCount.toInt()

        (totalMessages + CHAT_LOG_PAGE_SIZE - 1) / CHAT_LOG_PAGE_SIZE
    }

    private fun getFirstPageMessages(uuid: UUID, pageSize: Int): List<ChatMessage> {
        val cacheMessages = ChatMessageCache.getMessages(uuid)?.sortedByDescending { it.timestamp } ?: emptyList()
        val cacheSize = cacheMessages.size
        val dbNeeded = (pageSize - cacheSize).coerceAtLeast(0)

        val dbMessages = transaction {
            ChatMessages
                .selectAll().where { ChatMessages.sender eq uuid }
                .orderBy(ChatMessages.timestamp to SortOrder.DESC)
                .limit(dbNeeded, 0)
                .map {
                    ChatMessage(
                        id = it[ChatMessages.id],
                        sender = it[ChatMessages.sender],
                        timestamp = it[ChatMessages.timestamp],
                        content = it[ChatMessages.content]
                    )
                }
        }
        return mergeSortedLists(dbMessages, cacheMessages).take(pageSize)
    }

    private fun getPagedMessages(uuid: UUID, page: Int, pageSize: Int): List<ChatMessage> {
        val cacheMessages = ChatMessageCache.getMessages(uuid) ?: emptyList()
        val cacheSize = cacheMessages.size
        val pageIndex = (page - 1).coerceAtLeast(0)
        val adjustedOffset = (pageIndex * pageSize - cacheSize).coerceAtLeast(0).toLong()

        return transaction {
            ChatMessages
                .selectAll().where { ChatMessages.sender eq uuid }
                .orderBy(ChatMessages.timestamp to SortOrder.DESC)
                .limit(pageSize, adjustedOffset)
                .map {
                    ChatMessage(
                        id = it[ChatMessages.id],
                        sender = it[ChatMessages.sender],
                        timestamp = it[ChatMessages.timestamp],
                        content = it[ChatMessages.content]
                    )
                }
        }
    }


    private fun mergeSortedLists(
        list1: List<ChatMessage>,
        list2: List<ChatMessage>
    ): List<ChatMessage> {
        val result = mutableListOf<ChatMessage>()
        var i = 0
        var j = 0

        while (i < list1.size && j < list2.size) {
            if (list1[i].timestamp >= list2[j].timestamp) {
                result.add(list1[i])
                i++
            } else {
                result.add(list2[j])
                j++
            }
        }
        while (i < list1.size) result.add(list1[i++])
        while (j < list2.size) result.add(list2[j++])

        return result
    }


}