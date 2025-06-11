package me.wolfity.constants

object Permissions {

    private const val BASE: String  = "simplechatmod"

    const val MUTE_CHAT_PERMISSION: String = "$BASE.chatmute"
    const val MUTE_CHAT_BYPASS_PERMISSION: String = "$BASE.bypass"
    const val CHAT_FILTER_NOTIFY_PERMISSION: String = "$BASE.filter.notify"
    const val CHAT_FILTER_BYPASS_PERMISSION:  String = " $BASE.filter.bypass"
}