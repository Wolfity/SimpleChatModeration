package me.wolfity.commands

import me.wolfity.SimpleChatMod
import me.wolfity.constants.Permissions
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.bukkit.annotation.CommandPermission

class ChatCommands(val plugin: SimpleChatMod) {

    @Command("mutechat")
    @CommandPermission(Permissions.MUTE_CHAT_PERMISSION)
    fun onMuteChat(sender: Player) {
        plugin.chatManager.toggleChatMuted(sender)
    }

}