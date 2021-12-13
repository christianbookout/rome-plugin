package romeplugin.zoning;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.TimerTask;

public class LandEventListener implements Listener {
    private final LandControl controller;

    public static Material claimMaterial = Material.BRICK;
    public static long claimTimeoutMS = 20000;

    //private Timer claimTimer = new Timer();

    public LandEventListener(LandControl controller) {
        this.controller = controller;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        var block = event.getBlock();
        if (controller.canBreak(event.getPlayer(), block.getX(), block.getZ()))
            return;

        event.getPlayer().sendMessage("you can't break that, dumbass");
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        var block = event.getBlock();
        if (controller.canBreak(event.getPlayer(), block.getX(), block.getZ()))
            return;
        event.getPlayer().sendMessage("no :)");
        event.setCancelled(true);
    }

    private final HashMap<Player, Location> players = new HashMap<>();

    @EventHandler
    public void claimClicky(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null || e.getHand() == EquipmentSlot.HAND) {
            return;
        }
        //if player is clicking on a locked chest/door then don't let em
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && (e.getClickedBlock().getType().equals(Material.CHEST) || e.getClickedBlock().getState() instanceof Door)) {
            if (controller.canBreak(e.getPlayer(), e.getClickedBlock().getLocation().getBlockX(), e.getClickedBlock().getLocation().getBlockY())) {
                String formatting = ChatColor.RED.toString() + ChatColor.UNDERLINE.toString() + ChatColor.BOLD.toString();
                e.getPlayer().sendMessage(formatting + " woah " + ChatColor.RESET.toString() + " that is locked by " + e.getPlayer().getDisplayName());
                e.setCancelled(true);
            }
        }
        //if a player right clicks w/ the claim material then maybe claim some stuff!!
        else {
            e.getPlayer().getInventory().getItemInMainHand();
            if (e.getPlayer().getInventory().getItemInMainHand().getType().equals(claimMaterial) && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (players.containsKey(e.getPlayer())) {
                    Location lastLoc = players.get(e.getPlayer());
                    Location newLoc = e.getClickedBlock().getLocation();
                    //you can't claim the block you already clicked, silly
                    if (newLoc.getBlockX() == lastLoc.getBlockX() && newLoc.getBlockZ() == lastLoc.getBlockZ()) {
                        e.getPlayer().sendMessage("try claiming more than 1 block");
                    } else {
                        // TODO: check if claim is greater than max claim size . . .
                        ClaimLandCommand.claimLand(e.getPlayer(), lastLoc.getBlockX(), lastLoc.getBlockZ(), newLoc.getBlockX(), newLoc.getBlockZ());
                    }
                    players.remove(e.getPlayer());
                } else {
                    players.put(e.getPlayer(), e.getClickedBlock().getLocation());
                    e.getPlayer().sendMessage("claim started @ (" + e.getClickedBlock().getLocation().getX() + ", " + e.getClickedBlock().getLocation().getZ() + ").");
                    //if (claimTimeoutMS != 0)
                    //    claimTimer.schedule(new PlayerUnclaimTimer(e.getPlayer()), claimTimeoutMS);
                }
            }
        }
    }

    //funny
    class PlayerUnclaimTimer extends TimerTask {
        private final Player toRemove;

        PlayerUnclaimTimer(Player toRemove) {
            this.toRemove = toRemove;
        }

        @Override
        public void run() {
            players.remove(toRemove);
            toRemove.sendMessage("claim timed out");
        }
    }
}


