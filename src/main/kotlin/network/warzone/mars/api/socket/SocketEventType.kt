package network.warzone.mars.api.socket

enum class SocketEventType {
    MATCH_LOAD,
    MATCH_START,
    MATCH_END,
    CORE_LEAK,
    CORE_DAMAGE,
    DESTROYABLE_DAMAGE,
    DESTROYABLE_DESTROY,
    FLAG_CAPTURE,
    FLAG_PICKUP,
    FLAG_DROP,
    FLAG_DEFEND,
    WOOL_CAPTURE,
    WOOL_PICKUP,
    WOOL_DROP,
    WOOL_DEFEND,
    CONTROL_POINT_CAPTURE,
    PLAYER_RECORD_BREAK,
    PLAYER_DEATH,
    KILLSTREAK,
    PLAYER_CHAT,
    PARTY_JOIN,
    PARTY_LEAVE
}