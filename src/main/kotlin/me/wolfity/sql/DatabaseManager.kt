package me.wolfity.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseManager {

    fun init() {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite:plugins/SimpleChatMod/database.db"
            driverClassName = "org.sqlite.JDBC"
            maximumPoolSize = 5
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_SERIALIZABLE"
        }

        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(PlayerRegistry, ChatMessages)
        }
    }


}