package me.wolfity.commands

import me.wolfity.util.style
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.bukkit.exception.BukkitExceptionHandler
import revxrsal.commands.exception.MissingArgumentException
import revxrsal.commands.exception.NoPermissionException
import revxrsal.commands.node.ParameterNode

class LampExceptionHandler : BukkitExceptionHandler() {

    override fun onNoPermission(e: NoPermissionException, actor: BukkitCommandActor) {
        actor.sender().sendMessage(style("<red>No Permissions!"))
    }

    override fun onMissingArgument(
        e: MissingArgumentException,
        actor: BukkitCommandActor,
        parameter: ParameterNode<BukkitCommandActor?, *>
    ) {
        if (parameter.command().permission().isExecutableBy(actor)) {
            actor.sender().sendMessage(style("<red>Invalid Usage: ${e.command().usage()}"))
        } else {
            actor.sender().sendMessage(style("<red>No Permissions!"))
        }
    }

}