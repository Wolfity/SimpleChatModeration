package me.wolfity.reports

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.wolfity.db.ChatReports
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert


class ChatReportManager {

    suspend fun createReport(reporter: UUID, reported: UUID, reason: String): ChatReport = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        transaction {
            ChatReports.insert {
                it[ChatReports.reporter] = reporter
                it[ChatReports.reported] = reported
                it[ChatReports.timestamp] = timestamp
                it[ChatReports.reason] = reason
            }
        }
        ChatReport(reporter = reporter, reported = reported, timeStamp = timestamp, reason = reason)
    }

    suspend fun getAllReports(): List<ChatReport> = withContext(Dispatchers.IO) {
        transaction {
            ChatReports
                .selectAll()
                // Newest reports add the back of the stack
                .orderBy(ChatReports.timestamp to SortOrder.ASC)
                .map {
                    ChatReport(
                        id = it[ChatReports.id],
                        reporter = it[ChatReports.reporter],
                        reported = it[ChatReports.reported],
                        reason = it[ChatReports.reason],
                        timeStamp = it[ChatReports.timestamp]
                    )
                }
        }
    }

    suspend fun getReportCount(): Int = withContext(Dispatchers.IO) {
        transaction {
            ChatReports.selectAll().count()
        }.toInt()
    }

    suspend fun getReportsByReporter(uuid: UUID): List<ChatReport> = withContext(Dispatchers.IO) {
        transaction {
            ChatReports
                .selectAll().where { ChatReports.reporter eq uuid }
                .orderBy(ChatReports.timestamp to SortOrder.ASC)
                .map {
                    ChatReport(
                        id = it[ChatReports.id],
                        reporter = it[ChatReports.reporter],
                        reported = it[ChatReports.reported],
                        reason = it[ChatReports.reason],
                        timeStamp = it[ChatReports.timestamp]
                    )
                }
        }
    }

    suspend fun getReportsAgainstPlayer(uuid: UUID): List<ChatReport> = withContext(Dispatchers.IO) {
        val oneHourAgo = System.currentTimeMillis() - 60 * 60 * 1000 // last hour in ms

        transaction {
            ChatReports
                .selectAll().where {
                    (ChatReports.reported eq uuid) and (ChatReports.timestamp greaterEq oneHourAgo)
                }
                .orderBy(ChatReports.timestamp to SortOrder.ASC)
                .map {
                    ChatReport(
                        id = it[ChatReports.id],
                        reporter = it[ChatReports.reporter],
                        reported = it[ChatReports.reported],
                        reason = it[ChatReports.reason],
                        timeStamp = it[ChatReports.timestamp],
                    )
                }
        }
    }

    /**
     * Checks if [reporter] has made any reports against [reported] within the last [timeAgo]
     */
    suspend fun hasReportFromTo(reporter: UUID, reported: UUID, minutesAgo: Long): Boolean = withContext(Dispatchers.IO) {
        val cutoffTimestamp = System.currentTimeMillis() - (minutesAgo * 60_000)
        transaction {
            ChatReports.selectAll().where {
                (ChatReports.reporter eq reporter) and (ChatReports.reported eq reported) and (ChatReports.timestamp greaterEq cutoffTimestamp)
            }.limit(1).any()
        }
    }

    suspend fun resolveReport(id: Long): Boolean = withContext(Dispatchers.IO) {
        transaction {
            ChatReports.deleteWhere { ChatReports.id eq id } > 0
        }
    }

    /**
     * Resolves (closes) all reports against [reported] and returns the amount of reports that were deleted.
     */
    suspend fun resolveAllAgainst(reported: UUID) : Int = withContext(Dispatchers.IO) {
        transaction {
            ChatReports.deleteWhere { ChatReports.reported eq reported }
        }
    }
}