package network.warzone.mars.player.achievements.variants

import network.warzone.mars.api.socket.models.PlayerUpdateEvent
import network.warzone.mars.api.socket.models.PlayerUpdateReason
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementEmitter
import org.bukkit.event.EventHandler

class FlagCaptureAchievement(
    val captures: Int,
    override val emitter: AchievementEmitter) : AchievementAgent
{
    @EventHandler
    fun onProfileUpdate(event: PlayerUpdateEvent) {
        if (event.update.reason != PlayerUpdateReason.FLAG_PLACE) return
        val playerProfile = event.update.updated
        if (playerProfile.stats.objectives.flagCaptures >= captures) {
            emitter.emit(event.update.updated.name)
        }
    }
}