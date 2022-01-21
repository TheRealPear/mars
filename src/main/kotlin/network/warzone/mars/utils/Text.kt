package network.warzone.mars.utils

import net.time4j.ClockUnit
import network.warzone.mars.Mars
import network.warzone.mars.player.feature.LevelColorService
import network.warzone.mars.player.feature.PlayerService
import network.warzone.mars.player.models.PlayerProfile
import network.warzone.mars.punishment.commands.PunishCommands
import network.warzone.mars.punishment.models.Punishment
import network.warzone.mars.punishment.models.PunishmentKind
import network.warzone.mars.punishment.models.StaffNote
import network.warzone.mars.rank.models.Rank
import network.warzone.mars.tag.models.Tag
import org.bukkit.ChatColor
import org.bukkit.ChatColor.RED
import org.bukkit.ChatColor.translateAlternateColorCodes
import tc.oc.pgm.lib.net.kyori.adventure.platform.bukkit.BukkitAudiences
import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.*
import tc.oc.pgm.lib.net.kyori.adventure.text.TextComponent
import tc.oc.pgm.lib.net.kyori.adventure.text.event.ClickEvent
import tc.oc.pgm.lib.net.kyori.adventure.text.event.HoverEvent
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor
import tc.oc.pgm.lib.net.kyori.adventure.text.format.TextDecoration
import tc.oc.pgm.lib.net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import java.time.Duration
import java.util.*

val AUDIENCE_PROVIDER: BukkitAudiences = BukkitAudiences.create(Mars.get())

fun Rank.asTextComponent(editable: Boolean = true): TextComponent {
    val translatedPrefix = (this.prefix ?: "${RED}None").color()

    var hover = text()
        .append(
            text(this.name, NamedTextColor.GOLD, TextDecoration.UNDERLINED),
            text("\n\n")
        )
        .append { createStandardLabelled("Display Name", this.displayName ?: "None") }
        .append { createUncoloredLabelled("Prefix", translatedPrefix) }
        .append { createNumberedLabelled("Priority", this.priority) }
        .append { createBooleanLabelled("Staff", this.staff) }
        .append { createBooleanLabelled("Default", this.applyOnJoin) }
        .append { createNumberedLabelled("Permissions", this.permissions.size) }

    if (editable) hover =
        hover.append { text("\n") }.append { text("Click to edit", NamedTextColor.LIGHT_PURPLE, TextDecoration.ITALIC) }

    var finalComponent = if (this.prefix != null) {
        text()
            .append { text(this.prefix!!.color()) }
            .append { text(" (", NamedTextColor.GRAY) }
            .append { text(this.name, NamedTextColor.GRAY) }
            .append(text(")", NamedTextColor.GRAY))
    } else {
        text(this.name, NamedTextColor.GRAY).toBuilder()
    }

    finalComponent = finalComponent.hoverEvent(HoverEvent.showText(hover.build()))

    if (editable) finalComponent = finalComponent.clickEvent(ClickEvent.suggestCommand("/rank edit ${this.name}"))

    return finalComponent.build()
}

fun Punishment.asTextComponent(revertable: Boolean = true): TextComponent {
    var hover = text()
        .append(
            text("${this.action.kind.color}${this.reason.name} (${this.offence}) – ${this.action.kind.noun} (${this.action.formatLength()})"),
            newline(),
            newline()
        )
        .append { createUncoloredLabelled("Issued by", this.punisher?.name ?: "CONSOLE") }
        .append { createUncoloredLabelled("Issued at", "${this.issuedAt} (${this.issuedAt.getTimeAgo()})") }
        .append { createUncoloredLabelled("Expires", if (action.isPermanent()) "Never" else expiresAt.toString()) }
//        .append { createNumberedLabelled("Known IPs", this.targetIps.size) }

    if (this.note != null) hover = hover.append { createStandardLabelled("Note", this.note) }

    hover = hover
        .append { createBooleanLabelled("Silent", this.silent) }
        .append { createBooleanLabelled("Active", this.isActive) }
        .append { createBooleanLabelled("Reverted", this.reversion != null) }

    if (this.reversion != null) hover = hover.append { newline() }
        .append { createUncoloredLabelled("Reverted by", this.reversion.reverter.name) }
        .append {
            createUncoloredLabelled(
                "Reverted at",
                "${Date(this.reversion.revertedAt)} (${Date(this.reversion.revertedAt).getTimeAgo()})"
            )
        }
        .append { createUncoloredLabelled("Reversion reason", this.reversion.reason) }

    if (revertable && this.reversion == null) hover.append(newline(), text("Click to revert", NamedTextColor.LIGHT_PURPLE, TextDecoration.ITALIC))

    var finalComponent =
        text("[", NamedTextColor.GRAY)
            .append(text(this.issuedAt.getTimeAgo().toUpperCase(), NamedTextColor.GRAY))
            .append(text("]", NamedTextColor.GRAY))
            .append(space())
            .append(text("${if (this.isActive) this.action.kind.color else ChatColor.GRAY}${if (this.isReverted) ChatColor.STRIKETHROUGH else ""}${this.action.kind.verb.toUpperCase()}"))
            .append(space())
            .append(text(this.target.name, NamedTextColor.WHITE))

    finalComponent = finalComponent.hoverEvent(HoverEvent.showText(hover.build()))

    if (revertable && this.reversion == null) finalComponent = finalComponent.clickEvent(ClickEvent.runCommand("/revertp ${this._id}"))

    return finalComponent
}

fun PlayerService.PlayerAltResponse.asTextComponent(): TextComponent {
    val isMuted = this.punishments.find { it.action.kind == PunishmentKind.MUTE && it.isActive } != null
    val isBanned = this.punishments.find { it.action.isBan() && it.isActive } != null
    val color = if (isBanned) NamedTextColor.RED else if (isMuted) NamedTextColor.YELLOW else NamedTextColor.GRAY

    val hover = PunishCommands.createPlayerLore(player, this.punishments)
    val component = text(this.player.name, color).hoverEvent(
        HoverEvent.showText(
            PlainTextComponentSerializer.plainText().deserialize(hover.joinToString("\n"))
        )
    )
    return component
}

fun StaffNote.asTextComponent(player: String, deletable: Boolean): TextComponent {
    var hover = text()
        .append { createNumberedLabelled("ID", this.id) }
        .append { createUncoloredLabelled("Added at", "${this.createdAt} (${this.createdAt.getTimeAgo()})") }
        .append { createStandardLabelled("Author", this.author.name) }

    if (deletable) hover = hover.append { newline() }.append { text("Click to delete", NamedTextColor.LIGHT_PURPLE) }

    var finalComponent = text("${this.id}.", NamedTextColor.AQUA)
        .append { space() }
        .append { text(this.content, NamedTextColor.YELLOW) }
        .append { space() }
        .append { text("(${this.author.name})", NamedTextColor.GRAY) }
        .hoverEvent(HoverEvent.showText(hover.build()))

    if (deletable) finalComponent = finalComponent.clickEvent(ClickEvent.suggestCommand("/notes $player del $id"))

    return finalComponent
}

fun Tag.asTextComponent(complex: Boolean = false): TextComponent {
    return if (complex) {
        text()
            .append { text(this.display.color()) }
            .append { text(" (", NamedTextColor.GRAY) }
            .append { text(this.name, NamedTextColor.GRAY) }
            .append { text(")", NamedTextColor.GRAY) }
            .build()
    } else {
        text(this.display.color())
    }
}

fun Boolean.asTextComponent(): TextComponent {
    return if (this) {
        text("Yes", NamedTextColor.GREEN)
    } else {
        text("No", NamedTextColor.RED)
    }
}

/**
 *        val differenceDisplay: String = difference.joinToString("\n") {
"$YELLOW${it.name} $DARK_GRAY:: $WHITE$BOLD${it.source} $RESET$GRAY-> $WHITE$BOLD${it.new}"
}
 */
fun Difference.asTextComponent(): TextComponent {
    return text()
        .append { text(this.name, NamedTextColor.YELLOW) }
        .append { text(" :: ", NamedTextColor.DARK_GRAY) }
        .append { text(this.source, NamedTextColor.WHITE, TextDecoration.BOLD) }
        .append { text(" -> ", NamedTextColor.GRAY) }
        .append { text(this.new, NamedTextColor.WHITE, TextDecoration.BOLD) }
        .build()
}

fun createStandardLabelled(label: String, value: String): Component {
    return Component.join(
        text(""),
        text("$label: ", NamedTextColor.GRAY),
        text(value, NamedTextColor.YELLOW),
        text("\n")
    )
}

fun createNumberedLabelled(label: String, value: Int): Component {
    return Component.join(
        text(""),
        text("$label: ", NamedTextColor.GRAY),
        text(value.toString(), NamedTextColor.WHITE, TextDecoration.BOLD),
        text("\n")
    )
}

fun createBooleanLabelled(label: String, value: Boolean): Component {
    return Component.join(
        text(""),
        text("$label: ", NamedTextColor.GRAY),
        value.asTextComponent(),
        text("\n")
    )
}

fun createUncoloredLabelled(label: String, value: String): Component {
    return Component.join(
        text(""),
        text("$label: ", NamedTextColor.GRAY),
        text(value),
        text("\n")
    )
}

fun String.color(): String {
    return translateAlternateColorCodes('&', this)
}

fun Date.getTimeAgo(): String {
    val calendar = Calendar.getInstance()
    calendar.time = this

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val currentCalendar = Calendar.getInstance()

    val currentYear = currentCalendar.get(Calendar.YEAR)
    val currentMonth = currentCalendar.get(Calendar.MONTH)
    val currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH)
    val currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = currentCalendar.get(Calendar.MINUTE)

    return if (year < currentYear) {
        val interval = currentYear - year
        if (interval == 1) "$interval year ago" else "$interval years ago"
    } else if (month < currentMonth) {
        val interval = currentMonth - month
        if (interval == 1) "$interval month ago" else "$interval months ago"
    } else if (day < currentDay) {
        val interval = currentDay - day
        if (interval == 1) "$interval day ago" else "$interval days ago"
    } else if (hour < currentHour) {
        val interval = currentHour - hour
        if (interval == 1) "$interval hour ago" else "$interval hours ago"
    } else if (minute < currentMinute) {
        val interval = currentMinute - minute
        if (interval == 1) "$interval minute ago" else "$interval minutes ago"
    } else {
        "a moment ago"
    }
}

// only displays days and hours ("7d 3h") - used for punishment lengths
fun Duration.format(): String {
    val dur = net.time4j.Duration.of(this.seconds, ClockUnit.SECONDS)
    val interim = net.time4j.Duration.formatter("[##D]'d' [##h]'h'").format(dur.toTemporalAmount()).split(" ")
    val formatted = mutableListOf<String>()
    interim.forEach { if (it.length > 1) formatted.add(it) }
    return formatted.joinToString("")
}

// only displays hours, minutes and seconds ("7h 2m 23s") - used for playtime
fun Duration.conciseFormat(): String {
    return this.toString()
        .substring(2)
        .replace("(\\d[HMS])(?!$)".toRegex(), "$1 ")
        .replace("\\.\\d+".toRegex(), "")
        .toLowerCase()
}

fun String.chunkedWords(size: Int): List<String> {
    val words = this.split(' ')
    val chunked = words.chunked(size)
    return chunked.map { it.joinToString(" ") }
}

fun getPlayerLevelAsComponent(profile: PlayerProfile): Component {
    return text(
        "[${profile.stats.level}]",
        LevelColorService.chatColorFromLevel(profile.stats.level)
    )
}