package me.wolfity.util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
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

fun Player.sendStyled(message: String) {
    this.sendMessage(style(message))
}

fun style(message: String) : Component  {
    return miniMessage.deserialize(message)
}

fun Long.toSeconds() : Int {
    return this.toInt() * 20
}

fun formatTime(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm")
        .withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(timestamp))
}