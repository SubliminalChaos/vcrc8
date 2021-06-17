package me.waqe.plugin

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class App : JavaPlugin(), Listener {
    //    companion object {
        var contents: Array<ItemStack?>? = null
//    }
    var invs: MutableList<Inventory> = ArrayList()
    private var itemIndex = 0

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
    }

    override fun onDisable() {}

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        if (label.equals("gamble", ignoreCase = true)) {
            if (sender !is Player) {
                sender.sendMessage("No gambling for the console :(")
                return true
            }
            val player = sender
            val fee = ItemStack(Material.DIAMOND)
            fee.amount = 3
            if (player.inventory.itemInMainHand.isSimilar(fee)) {
                player.inventory.removeItem(fee)
                // spin that GUI!!!
                spin(player)
                return true
            }
            player.sendMessage(ChatColor.DARK_RED.toString() + "You need 3 diamonds to gamble!")
            return true
        }
        return false
    }

    fun shuffle(inv: Inventory) {
        if (contents == null) {
            val items = arrayOfNulls<ItemStack>(10)
            items[0] = ItemStack(Material.COARSE_DIRT, 32)
            items[1] = ItemStack(Material.DIAMOND, 3)
            items[2] = ItemStack(Material.IRON_INGOT, 64)
            items[3] = ItemStack(Material.NETHER_STAR, 8)
            items[4] = ItemStack(Material.BEE_SPAWN_EGG, 1)
            items[5] = ItemStack(Material.QUARTZ_BLOCK, 64)
            items[6] = ItemStack(Material.DIAMOND, 3)
            items[7] = ItemStack(Material.ACACIA_WOOD, 12)
            items[8] = ItemStack(Material.DIAMOND, 3)
            items[9] = ItemStack(Material.BARRIER, 1)
            contents = items
        }
        val startingIndex = ThreadLocalRandom.current().nextInt(contents!!.size)
        for (index in 0 until startingIndex) {
            for (itemstacks in 9..17) {
                inv.setItem(
                    itemstacks,
                    contents!![(itemstacks + itemIndex) % contents!!.size]
                )
            }
            itemIndex++
        }

        // Customize
        val item = ItemStack(Material.HOPPER)
        val meta = item.itemMeta
        meta!!.setDisplayName(ChatColor.DARK_GRAY.toString() + "|")
        item.itemMeta = meta
        inv.setItem(4, item)
    }

    fun spin(player: Player) {
        val inv = Bukkit.createInventory(null, 27, ChatColor.GOLD.toString() + "" + ChatColor.BOLD + "Goodluck!")
        shuffle(inv)
        invs.add(inv)
        player.openInventory(inv)
        val seconds = 7.0 + (12.0 - 7.0) * 1.0

        object : BukkitRunnable() {
            var delay = 0.0
            var ticks = 0
            var done = false
            override fun run() {
                if (done) return
                ticks++
                delay += 1 / (20 * seconds)
                if (ticks > delay * 10) {
                    ticks = 0
                    for (itemstacks in 9..17) inv.setItem(
                        itemstacks,
                        contents!![(itemstacks + itemIndex) % contents!!.size]
                    )
                    itemIndex++
                    if (delay >= .5) {
                        done = true
                        object : BukkitRunnable() {
                            override fun run() {
                                val item = inv.getItem(13)
                                player.inventory.addItem(item)
                                player.updateInventory()
                                player.closeInventory()
                                cancel()
                            }
                        }.runTaskLater(getPlugin(App::class.java), 50)
                        cancel()
                    }
                }
            }

        }.runTaskTimer(this, 0, 1)

        @EventHandler
        fun onClick(event: InventoryClickEvent) {
            if (!invs.contains(event.inventory)) return
            event.isCancelled = true
            return
        }

    }
}