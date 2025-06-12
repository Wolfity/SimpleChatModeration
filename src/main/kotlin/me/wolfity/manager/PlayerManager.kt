package me.wolfity.manager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.wolfity.sql.PlayerRegistry
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

data class PlayerData(val uuid: UUID, val name: String)

class PlayerManager {

    suspend fun registerPlayer(uuid: UUID, name: String) = withContext(Dispatchers.IO) {
        transaction {
            PlayerRegistry.insertIgnore {
                it[PlayerRegistry.uuid] = uuid
                it[PlayerRegistry.name] = name
            }
        }
    }

    suspend fun getDataByName(name: String): PlayerData? {
        return withContext(Dispatchers.IO) {
            transaction {
                PlayerRegistry
                    .selectAll().where { PlayerRegistry.name eq name }
                    .map { PlayerData(it[PlayerRegistry.uuid], it[PlayerRegistry.name]) }
                    .singleOrNull()
            }
        }
    }

    suspend fun getDataByUUID(uuid: UUID): PlayerData? {
        return withContext(Dispatchers.IO) {
            transaction {
                PlayerRegistry
                    .selectAll().where { PlayerRegistry.uuid eq uuid }
                    .map { PlayerData(it[PlayerRegistry.uuid], it[PlayerRegistry.name]) }
                    .singleOrNull()
            }
        }
    }
}