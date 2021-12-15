package romeplugin.zoning;

import net.md_5.bungee.api.ChatColor;
import romeplugin.database.ClaimEntry;
import romeplugin.database.SQLConn;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Gate;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class LandEventListener implements Listener {
    private final LandControl controller;
    public static final Material DEFAULT_MATERIAL = Material.BRICK;

    private final Material claimMaterial;
    private final long claimTimeoutMS;

    private final Timer claimTimer = new Timer();

    //Hashmap of players and their claim start location to allow for claiming with a claim item (default: DEFAULT_MATERIAL)
    private final HashMap<Player, Location> players = new HashMap<>();

    //the materials people can't right-click in a claim/the city
    private final Material[] nonClickables = new Material[] {
        Material.CHEST,
        Material.BARREL,
        Material.ANVIL,
        Material.BEACON,
        Material.BLAST_FURNACE,
        Material.FURNACE,
        Material.SMOKER,
        Material.CHEST_MINECART,
        Material.ITEM_FRAME,
        Material.ARMOR_STAND
    };

    //materials that shouldn't be allowed to flow into the city/a claim
    private final Material[] nonFlowables = new Material[] {
        Material.WATER,
        Material.LAVA
    };

    public LandEventListener(LandControl controller, Material claimMaterial, long claimTimeoutMS) {
        this.controller = controller;
        this.claimMaterial = claimMaterial;
        this.claimTimeoutMS = claimTimeoutMS;
    }

    //prevent people from breaking blocks in protected areas
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        var block = event.getBlock();
        if (controller.canBreak(event.getPlayer(), block.getLocation()))
            return;

        event.getPlayer().sendMessage("you can't break that, dumbass");
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        var block = event.getBlock();
        if (controller.canBreak(event.getPlayer(), block.getLocation()))
            return;
        event.getPlayer().sendMessage("no :)");
        event.setCancelled(true);
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent e) {
        if (controller.inCity(e.getLocation())) 
            e.blockList().clear();
        
        else 
            e.blockList().removeIf(block -> controller.inCity(block.getLocation()));
        
    }
    
    //prevent item frames/paintings from being broken by entities like skellingtons or boats in the city
    @EventHandler
    public void hangingItemBroken(HangingBreakEvent e) {
        if (e.getCause() == RemoveCause.PHYSICS) 
            return;

        Location loc = e.getEntity().getLocation();
        if (controller.inCity(loc))
            e.setCancelled(true);
    }

    //don't let players break hanging items w/ a bow or something in a claim/city
    @EventHandler
    public void hangingItemBroken(HangingBreakByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) 
            return;

        Player remover = (Player) e.getRemover();
        if (!controller.canBreak(remover, e.getEntity().getLocation())) {
            remover.sendMessage("don't break the hanging thing");
            e.setCancelled(true);
        }
    }

    //When a dispenser outside the city tries to dispense something into the city, then flip it around and dispense (cause that's kinda funny)
    @EventHandler
    public void blockDispensedEvent(BlockDispenseEvent e){
        if (e.getBlock().getBlockData() instanceof Directional) {
            Directional direction = (Directional) e.getBlock().getBlockData();

            int newX = e.getBlock().getLocation().getBlockX() + direction.getFacing().getModX();
            int newZ = e.getBlock().getLocation().getBlockZ() + direction.getFacing().getModZ();


            if (!controller.inCity(e.getBlock().getLocation()) && controller.inCity(newX, newZ)) {
                direction.setFacing(direction.getFacing().getOppositeFace());
            }
        }
    }

    //don't let players steal items from an item frame in a claim/city
    @EventHandler
    public void frameItemStolen(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && (e.getEntity() instanceof ItemFrame)) {
            Player damager = (Player) e.getDamager();
            if (!controller.canBreak(damager, e.getEntity().getLocation())) {
                damager.sendMessage("filthy thief");
                e.setCancelled(true);
            }
        }
    }

    //Only applies to water and lava (and dragon eggs, but that's okay)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockFromTo(BlockFromToEvent event) {
        //dragon egg is trying to enter claimed blocks ig
        if (!Arrays.asList(nonFlowables).contains(event.getToBlock().getType())) 
            return;

        Location fromLoc = event.getBlock().getLocation();
        Location toLoc = event.getToBlock().getLocation();

        boolean toLocInCity = controller.inCity(toLoc);
        boolean fromLocInCity = controller.inCity(fromLoc);
        //liquid can't flow from outside of city into the city
        if (toLocInCity && !fromLocInCity) {
            event.setCancelled(true);
            return;
        }
        ClaimEntry fromClaim = SQLConn.getClaim(fromLoc);
        ClaimEntry toClaim = SQLConn.getClaim(toLoc);

        //If liquid is flowing from the wild into a claim, stop it 
        if (fromClaim == null && toClaim != null) {
            event.setCancelled(true);
            return;
        }

        //If liquid is moving from a claim into the city, don't let it 
        if (toLocInCity && fromClaim != null) {
            event.setCancelled(true);
            return;
        }

        //liquid can't flow from one claim to someone else's adjacent claim
        if (!fromClaim.owner.equals(toClaim.owner)) {
            event.setCancelled(true);
            return;
        }
        
    }
    //TODO make it so people can't open fence gates, click on buttons, press levers, etc in the city
    @EventHandler
    public void claimClicky(PlayerInteractEvent e) { 
        if (e.getClickedBlock() == null || e.getHand() == EquipmentSlot.HAND) {
            return;
        }
        Location newLoc = e.getClickedBlock().getLocation();
        //if player is clicking on a locked chest/door then don't let em
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && (Arrays.asList(nonClickables).contains(e.getClickedBlock().getType()) || e.getClickedBlock().getState() instanceof Door)) {
            
            if (!controller.canBreak(e.getPlayer(), newLoc)) {

                String formatting = ChatColor.RED.toString() + ChatColor.UNDERLINE.toString() + ChatColor.BOLD.toString();
                e.getPlayer().sendMessage(formatting + " woah " + ChatColor.RESET.toString() + " that is locked by " + e.getPlayer().getDisplayName());
                e.setCancelled(true);
            }
        }
        //if a player right clicks w/ the claim material then maybe claim some stuff!!
        else if (e.getPlayer().getInventory().getItemInMainHand().getType().equals(claimMaterial) && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (players.containsKey(e.getPlayer())) {
                Location lastLoc = players.get(e.getPlayer());

                //you can't claim the block you already clicked, silly
                if (newLoc.getBlockX() == lastLoc.getBlockX() && newLoc.getBlockZ() == lastLoc.getBlockZ()) {
                    e.getPlayer().sendMessage("try claiming more than 1 block");

                } else { //TODO: check if claim is greater than max claim size . . .
                    ClaimLandCommand.claimLand(e.getPlayer(), lastLoc.getBlockX(), lastLoc.getBlockZ(), newLoc.getBlockX(), newLoc.getBlockZ());
                }

                players.remove(e.getPlayer());

            } else {
                players.put(e.getPlayer(), e.getClickedBlock().getLocation());
                e.getPlayer().sendMessage("claim started @ (" + newLoc.getBlockX() + ", " + newLoc.getBlockZ() + ").");

                if (claimTimeoutMS != 0)
                    claimTimer.schedule(new PlayerUnclaimTimer(e.getPlayer(), newLoc), claimTimeoutMS);
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
            removePlayer.sendMessage(ChatColor.RED.toString() + "claim @ (" + removeLocation.getBlockX() + ", " + removeLocation.getBlockZ() + ") timed out");
        }
    }
}


