package me.wolfity.constants

object Permissions {

    private const val BASE: String = "simplechatmod"

    const val MUTE_CHAT_PERMISSION: String = "$BASE.chatmute"
    const val MUTE_CHAT_BYPASS_PERMISSION: String = "$BASE.bypass"
    const val CHAT_FILTER_NOTIFY_PERMISSION: String = "$BASE.filter.notify"
    const val CHAT_FILTER_BYPASS_PERMISSION: String = "$BASE.filter.bypass"
    const val VIEW_CHAT_LOG_PERMISSION: String = "$BASE.chatlogs.view"
    const val CHAT_REPORT_HANDLE_PERMISSION: String = "$BASE.chatreports.handle"
    const val SLOW_CHAT: String = "$BASE.chat.slow"
    const val SLOW_CHAT_BYPASS: String = "$BASE.chat.slow.bypass"
    const val PLUGIN_INFO_PERMISSION: String = "$BASE.plugin.info"
}