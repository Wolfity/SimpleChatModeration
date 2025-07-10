package me.wolfity.files

import me.wolfity.plugin
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class CustomConfig(
    private val fileName: String
) {
    private lateinit var configFile: File
    lateinit var config: FileConfiguration
        private set

    init {
        load()
    }

    private fun load() {
        configFile = File(plugin.dataFolder, fileName)
        if (!configFile.exists()) {
            plugin.saveResource(fileName, false)
        }
        config = YamlConfiguration.loadConfiguration(configFile)
    }

    fun reload() {
        config = YamlConfiguration.loadConfiguration(configFile)
    }

    fun save() {
        config.save(configFile)
    }

    fun getLong(path: String): Long = config.getLong(path)
    fun getString(path: String): String = config.getString(path)!!
    fun getStringSafe(path: String) = config.getString(path)
    fun getInt(path: String): Int = config.getInt(path)
    fun getStringList(path: String): List<String> = config.getStringList(path)
    fun getBoolean(path: String): Boolean = config.getBoolean(path)
    fun set(path: String, value: Any) {
        this.config.set(path, value)
        save()
    }
}
