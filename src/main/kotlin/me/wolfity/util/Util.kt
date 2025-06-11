package me.wolfity.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player

val miniMessage = MiniMessage.miniMessage()

fun Player.sendStyled(message: String) {
    this.sendMessage(style(message))
}

fun style(message: String) : Component  {
    return miniMessage.deserialize(message)
}