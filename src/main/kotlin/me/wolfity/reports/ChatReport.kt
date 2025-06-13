package me.wolfity.reports

import java.util.UUID

data class ChatReport(
    val id: Long = -1,
    val reporter: UUID,
    val reported: UUID,
    val reason: String,
    val timeStamp: Long = System.currentTimeMillis()
)
