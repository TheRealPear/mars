package network.warzone.pgm.api.socket.models

import network.warzone.pgm.utils.KEvent
import java.util.*

enum class ChatChannel {
    GLOBAL,
    TEAM,
    STAFF
}

data class PlayerChatData(val playerId: UUID, val playerName: String, val playerPrefix: String, val channel: ChatChannel, val message: String)

data class PlayerChatEvent(val data: PlayerChatData) : KEvent()