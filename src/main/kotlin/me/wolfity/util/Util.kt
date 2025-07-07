package me.wolfity.util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.net.URL
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

val miniMessage = MiniMessage.miniMessage()

/** Launches a suspending task [block] on the IO thread. */
fun launchAsync(block: suspend CoroutineScope.() -> Unit): Job {
    return CoroutineScope(Dispatchers.IO).launch {
        block.invoke(this)
    }
}

/**
 * Builds an itemstack with the given configuration
 */
fun buildItem(material: Material, name: Component, builder: ItemBuilder.() -> Unit = {}): ItemStack {
    return ItemBuilder(material, name).apply(builder).build()
}

/**
 * Builds an itemstack with the given configuration.
 */
fun buildItem(material: Material, setup: ItemBuilder.() -> Unit = {}): ItemStack {
    return ItemBuilder(material).apply(setup).build()
}


fun Player.sendStyled(message: String) {
    this.sendMessage(style(message))
}

fun style(message: String) : Component  {
    return miniMessage.deserialize(message)
}

fun Long.toSeconds() : Int {
    return this.toInt() * 20
}

fun formatTime(timestamp: Long, includeSeconds: Boolean = false): String {
    val pattern = if (includeSeconds) "dd/MM HH:mm:ss" else "dd/MM HH:mm"
    val formatter = DateTimeFormatter.ofPattern(pattern)
        .withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(timestamp))
}

fun isValidUrl(urlStr: String): Boolean {
    return try {
        URL(urlStr).toURI()
        true
    } catch (e: Exception) {
        false
    }
}