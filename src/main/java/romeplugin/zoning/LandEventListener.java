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
import org.bukkit.inventory.EquipmentSlot;

import net.md_5.bungee.api.ChatColor;

public class LandEventListener implements Listener {
    private final LandControl controller;
    public static final Material DEFAULT_MATERIAL = Material.BRICK;

    private final Material claimMaterial;
    private final long claimTimeoutMS;

    private Timer claimTimer = new Timer();

    public LandEventListener(LandControl controller, Material claimMaterial, long claimTimeoutMS) {
        this.controller = controller;
        this.claimMaterial = claimMaterial;
        this.claimTimeoutMS = claimTimeoutMS;
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
                e.getPlayer().sendMessage( formatting + " woah " + ChatColor.RESET.toString() + " that is locked by " + e.getPlayer().getDisplayName());
                e.setCancelled(true);
            } 
        }   
        //if a player right clicks w/ the claim material then maybe claim some stuff!!
        else if (e.getPlayer().getInventory().getItemInMainHand() != null && e.getPlayer().getInventory().getItemInMainHand().getType().equals(claimMaterial) && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Location newLoc = e.getClickedBlock().getLocation();
            if (players.containsKey(e.getPlayer())) {
                Location lastLoc = players.get(e.getPlayer());
                //you can't claim the block you already clicked, silly
                if (newLoc.getBlockX() == lastLoc.getBlockX() && newLoc.getBlockZ() == lastLoc.getBlockZ()) {
                    e.getPlayer().sendMessage("try claiming more than 1 block");
                    players.remove(e.getPlayer());
                    return;
                } else { //TODO: check if claim is greater than max claim size . . .
                    ClaimLandCommand.claimLand(e.getPlayer(), lastLoc.getBlockX(), lastLoc.getBlockZ(), newLoc.getBlockX(), newLoc.getBlockZ());
                    players.remove(e.getPlayer());
                    return;
                }
            } else {
                players.put(e.getPlayer(), e.getClickedBlock().getLocation());
                e.getPlayer().sendMessage("claim started @ (" + newLoc.getBlockX() + ", " + newLoc.getBlockZ() + ").");
                if (claimTimeoutMS != 0) 
                    claimTimer.schedule(new PlayerUnclaimTimer(e.getPlayer(), newLoc), claimTimeoutMS);
                return;
            }
        }
    }
    //funny
    class PlayerUnclaimTimer extends TimerTask {
        private final Player removePlayer;
        private final Location removeLocation;
        PlayerUnclaimTimer(Player removePlayer, Location removeLocation) {
            this.removePlayer = removePlayer;
            this.removeLocation = removeLocation;
        }
        @Override
        public void run() {
            if (!players.get(removePlayer).equals(removeLocation)) 
                return;
            players.remove(removePlayer);
            removePlayer.sendMessage(ChatColor.RED.toString() + "claim @ ("+ removeLocation.getBlockX() + ", " + removeLocation.getBlockZ() + ") timed out");
        }
        
    }
}


