package me.wolfity.logging

import java.util.*

class ChatMessage(
    val id: Long = -1,
    val sender: UUID,
    val timestamp: Long = System.currentTimeMillis(),
    val content: String
) {
}