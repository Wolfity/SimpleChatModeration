package me.wolfity.sql

import org.jetbrains.exposed.sql.Table

object PlayerRegistry : Table("player_registry") {
    val uuid = uuid("sender").uniqueIndex()
    val name = varchar("name", 32).index()
    val skin = text("skin").nullable()
    override val primaryKey: PrimaryKey = PrimaryKey(uuid)
}

object ChatMessages : Table("chat_messages") {
    val id = long("id").autoIncrement()
    val sender = uuid("sender").references(PlayerRegistry.uuid).index()
    val timestamp = long("timestamp")
    val content = varchar("content", 255)
    override val primaryKey = PrimaryKey(id)
}

object ChatReports : Table("chat_reports") {
    val id = long("id").autoIncrement()
    val reporter = uuid("reporter").references(PlayerRegistry.uuid).index()
    val reported = uuid("reported").references(PlayerRegistry.uuid).index()
    val reason = varchar("reason", 32)
    val timestamp = long("timestamp")

    override val primaryKey = PrimaryKey(id)
}
