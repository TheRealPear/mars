package network.warzone.mars.match.tracker

import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.CoreDamageData
import network.warzone.mars.api.socket.models.CoreLeakData
import network.warzone.mars.match.models.Contribution
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import tc.oc.pgm.core.CoreBlockBreakEvent
import tc.oc.pgm.core.CoreLeakEvent
import kotlin.math.floor

class CoreTracker : Listener {

    @EventHandler
    fun onCoreDamage(event: CoreBlockBreakEvent) {
//        ApiClient.emit(OutboundEvent.CoreDamage, CoreDamageData(event.core.id, event.player.id))
    }

    @EventHandler
    fun onCoreLeak(event: CoreLeakEvent) {
        val totalBrokenBlocks = event.core.casingRegion.blocks.filter { !event.core.isObjectiveMaterial(it) }.size

        ApiClient.emit(OutboundEvent.CoreLeak, CoreLeakData(
            event.core.id,
            event.core.contributions
                .map { Contribution(
                    it.playerState.id,
                    it.percentage.toFloat(),
                    floor(it.percentage * totalBrokenBlocks).toInt()
                ) }
        ))
    }

}