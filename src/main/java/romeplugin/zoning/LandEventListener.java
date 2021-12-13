package romeplugin.zoning;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

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

import net.md_5.bungee.api.ChatColor;

public class LandEventListener implements Listener {
    private final LandControl controller;

    public static Material claimMaterial = Material.BRICK;
    public static long claimTimeoutMS = 20000;

    private Timer claimTimer = new Timer();

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
        if (e.getClickedBlock() == null) {
            return;
        }
        //if player is clicking on a locked chest/door then don't let em
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && (e.getClickedBlock().getType().equals(Material.CHEST) || e.getClickedBlock().getState() instanceof Door)) {
            if (controller.canBreak(e.getPlayer(), e.getClickedBlock().getLocation().getBlockX(), e.getClickedBlock().getLocation().getBlockY())) {
                String formatting = ChatColor.RED.toString() + ChatColor.UNDERLINE.toString() + ChatColor.BOLD.toString();
                e.getPlayer().sendMessage( formatting + " woah " + ChatColor.RESET.toString() + " that is locked by " + e.getPlayer().getDisplayName());
                e.setCancelled(true);
            } 
        }   
        //if a player right clicks w/ the claim material then maybe claim some stuff!!
        else if (e.getPlayer().getInventory().getItemInMainHand() != null && e.getPlayer().getInventory().getItemInMainHand().getType().equals(claimMaterial) && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (players.containsKey(e.getPlayer())) {
                Location lastLoc = players.get(e.getPlayer());
                Location newLoc = e.getClickedBlock().getLocation();
                //you can't claim the block you already clicked, silly
                if (newLoc.getBlockX() == lastLoc.getBlockX() && newLoc.getBlockY() == lastLoc.getBlockY() && newLoc.getBlockZ() == lastLoc.getBlockZ()) {
                    e.getPlayer().sendMessage("try claiming more than 1 block");
                    players.remove(e.getPlayer());
                    return;
                } else { //TODO: check if claim is greater than max claim size . . .
                    ClaimLandCommand.claimLand(e.getPlayer(), lastLoc.getBlockX(), lastLoc.getBlockY(), newLoc.getBlockX(), newLoc.getBlockY());
                    players.remove(e.getPlayer());
                    return;
                }
            } else {
                players.put(e.getPlayer(), e.getClickedBlock().getLocation());
                if (claimTimeoutMS != 0) 
                    claimTimer.schedule(new PlayerUnclaimTimer(e.getPlayer()), claimTimeoutMS);
                return;
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


