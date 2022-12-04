package network.warzone.mars.admin

import app.ashcon.intake.Command
import app.ashcon.intake.CommandException
import app.ashcon.intake.bukkit.parametric.annotation.Sender
import network.warzone.mars.Mars
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.utils.getRelativeTime
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import tc.oc.pgm.lib.cloud.commandframework.exceptions.NoPermissionException
import javax.annotation.Nullable

class AdminCommands {
    /**
     * View active XP multiplier: mars.xpmultiplier.view
     * View time & player who set XP multiplier: pgm.staff
     * Set XP multiplier to value 1-5: mars.xpmultiplier.set
     * Set XP multiplier to value >5: mars.xpmultiplier.setunlimited && mars.xpmultiplier.set
     */
    @Command(aliases = ["xpmultiplier", "booster"], desc = "Set an XP multiplier", usage = "[multiplier]", perms = ["mars.xpmultiplier.view"])
    fun onSetXPMultiplier(@Sender sender: CommandSender, @Nullable multiplier: Float?) {
        Mars.async {
            try {
                if (multiplier == null) {
                    val xpMultiplier = AdminService.getCurrentEvents()!!.xpMultiplier
                        ?: return@async sender.sendMessage("${ChatColor.RED}There is no XP multiplier active at the moment")
                    val player = xpMultiplier.player

                    var message = "${ChatColor.GREEN}An XP multiplier of ${ChatColor.YELLOW}${xpMultiplier.value}x ${ChatColor.GREEN}is active"
                    if (sender.hasPermission("pgm.staff")) message += ", set ${ChatColor.YELLOW}${xpMultiplier.updatedAt.getRelativeTime(null, true)}${ChatColor.GREEN}"
                    if (sender.hasPermission("pgm.staff") && player != null) message += " by ${ChatColor.YELLOW}${player.name}"

                    sender.sendMessage(message)
                } else {
                    if (!sender.hasPermission("mars.xpmultiplier.set")) throw CommandException("Insufficient permissions")
                    if (multiplier > 5f && !sender.hasPermission("mars.xpmultiplier.setunlimited")) throw CommandException("Insufficient permissions")
                    if (multiplier < 1) throw CommandException("XP multiplier cannot be less than one")
                    val events = AdminService.setXPMultiplier(multiplier, if (sender is Player) SimplePlayer(sender.uniqueId, sender.name) else null)
                    val xpMultiplier = events?.xpMultiplier ?: return@async sender.sendMessage("${ChatColor.GREEN}XP multiplier cleared")
                    sender.sendMessage("${ChatColor.GREEN}XP multiplier has been set to ${ChatColor.YELLOW}${xpMultiplier.value}x")
                    Bukkit.broadcastMessage("${ChatColor.GREEN}XP multiplier has been set to ${ChatColor.YELLOW}${xpMultiplier.value}x ${ChatColor.GREEN}by ${ChatColor.YELLOW}${sender.name}", "pgm.staff")
                }
            } catch (e: CommandException) {
                sender.sendMessage("${ChatColor.RED}${e.message}")
            }
        }
    }
}