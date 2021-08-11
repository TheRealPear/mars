package network.warzone.pgm.ranks.exceptions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.pgm.utils.FeatureException

data class RankConflictException(val name: String) : FeatureException() {

    override fun asTextComponent(): TextComponent {
        return Component.text("A rank by the name $name already exists.", NamedTextColor.RED)
    }

}