package romeplugin.zoning;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import romeplugin.database.ClaimEntry;
import romeplugin.database.SQLConn;

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
    private final Material[] nonClickables = new Material[]{
            Material.CHEST,
            Material.BARREL,
            Material.ANVIL,
            Material.BEACON,
            Material.BLAST_FURNACE,
            Material.FURNACE,
            Material.SMOKER,
            Material.CHEST_MINECART,
            Material.FURNACE_MINECART,
            Material.HOPPER,
            Material.HOPPER_MINECART,
            Material.BREWING_STAND,
            Material.ITEM_FRAME,
            Material.TRAPPED_CHEST,
            Material.ARMOR_STAND
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
        if (!controller.inSuburbs(block.getLocation())) return;
        if (controller.canBreak(event.getPlayer(), block.getLocation()))
            return;

        event.getPlayer().sendMessage(ChatColor.RED + "you can't break that, dumbass");
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        var block = event.getBlock();
        if (!controller.inSuburbs(block.getLocation())) return;
        if (controller.canBreak(event.getPlayer(), block.getLocation()))
            return;
        event.getPlayer().sendMessage(ChatColor.RED + "no :)");
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void blockCanBuild(BlockCanBuildEvent e) {
        Location location = e.getBlock().getLocation();
        if (!controller.inSuburbs(location)) return;

        if (e.getPlayer() == null) {
            return;
        }
        if (!controller.canBreak(e.getPlayer(), location)) {
            e.setBuildable(false);
            e.getPlayer().sendMessage(ChatColor.RED + "you can't build here");
        }
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent e) {
        e.blockList().removeIf(block -> controller.inCity(block.getLocation()));

    }

    //prevent item frames/paintings from being broken by entities like skellingtons or boats in the city
    @EventHandler
    public void hangingItemBroken(HangingBreakEvent e) {
        if (e.getCause() != RemoveCause.PHYSICS)
            return;

        Location loc = e.getEntity().getLocation();
        if (controller.inCity(loc) || SQLConn.getClaim(loc) != null)
            e.setCancelled(true);
    }

    //don't let players or mobs break hanging items w/ a bow or something in a claim/city
    @EventHandler
    public void hangingItemBroken(HangingBreakByEntityEvent e) {
        Location location = e.getEntity().getLocation();
        if (!controller.inSuburbs(location)) return;

        //If a mob or something else tries to break an item frame and they are in a city or a claim then stop them
        if (!(e.getRemover() instanceof Player)) {
            if (controller.inCity(location) 
                || SQLConn.getClaim(location) != null) {

                e.setCancelled(true);
            }
            return;
        }

        //If a player's breaking the frame and they aren't allowed then don't let them
        Player remover = (Player) e.getRemover();
        if (!controller.canBreak(remover, e.getEntity().getLocation())) {
            remover.sendMessage(ChatColor.RED + "don't break the hanging thing");
            e.setCancelled(true);
        }
    }

    //don't let people place paintings in a city or claim
    @EventHandler
    public void hangingItemPlaced(HangingPlaceEvent e) {
        Location location = e.getBlock().getLocation();
        if (!controller.inSuburbs(location)) return;
        if (!controller.canBreak(e.getPlayer(), location)) {
            e.setCancelled(true);
        }
    }

    //When a dispenser outside the city tries to dispense something into the city, then cancel it
    @EventHandler
    public void blockDispensedEvent(BlockDispenseEvent e) {
        Block fromBlock = e.getBlock();

        if (!controller.inSuburbs(fromBlock.getLocation())) return;

        BlockData fromBlockData = e.getBlock().getBlockData();

        if (!(fromBlockData instanceof Dispenser)) return;

        Dispenser dispenser = (Dispenser) fromBlockData;

        Block toBlock = fromBlock.getRelative(((Directional) dispenser.getBlockData()).getFacing());
        boolean toBlockInCity = controller.inCity(toBlock.getLocation());

        if (toBlockInCity && !controller.inCity(fromBlock.getLocation())) {
            e.setCancelled(true);
            return;
        }

        ClaimEntry fromClaim = SQLConn.getClaim(fromBlock.getLocation());
        ClaimEntry toClaim = SQLConn.getClaim(toBlock.getLocation());
        //If a dispenser is going from no claim to a claim or other way around (and it's happening in a city) then cancel it
        if ((fromClaim == null && toClaim != null) || (toClaim == null && fromClaim != null && toBlockInCity)) {
            e.setCancelled(true);
            return;
        }

        //If a dispenser is dispensing from one claim into another claim with a different owner then cancel it
        if (fromClaim != null && toClaim != null && !fromClaim.owner.equals(toClaim.owner)) {
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void playerBucketFillEvent(PlayerBucketFillEvent e) {

        Player player = e.getPlayer();
        Location placePosition = e.getBlockClicked().getLocation();

        if (!controller.inSuburbs(placePosition)) return;

        if (!controller.canBreak(player, placePosition)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void playerBucketEmptyEvent(PlayerBucketEmptyEvent e) {
        
        Player player = e.getPlayer();
        Location placePosition = e.getBlockClicked().getLocation();

        if (!controller.inSuburbs(placePosition)) return;
        
        if (!controller.canBreak(player, placePosition)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void spongeAbsorbEvent(SpongeAbsorbEvent e) {
        //TODO
    }

    //don't let players steal items from an item frame in a claim/city
    @EventHandler
    public void frameItemStolen(EntityDamageByEntityEvent e) {
        Location location = e.getEntity().getLocation();

        if (!controller.inSuburbs(location)) return;

        if (!(e.getEntity() instanceof ItemFrame || e.getEntity() instanceof ArmorStand))
            return;

        if (controller.inCity(location)) {
            e.setCancelled(true);
            return;
        }

        if (e.getDamager() instanceof Player) {
            Player damager = (Player) e.getDamager();
            if (!controller.canBreak(damager, e.getEntity().getLocation())) {
                damager.sendMessage(ChatColor.RED + "filthy thief");
                e.setCancelled(true);
            }
        }
    }

    //Only applies to water and lava
    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (event.getBlock().getType() == Material.DRAGON_EGG) return;
        Location fromLoc = event.getBlock().getLocation();
        Location toLoc = event.getToBlock().getLocation();

        boolean toLocInCity = controller.inCity(toLoc);
        boolean fromLocInCity = controller.inCity(fromLoc);

        if (!controller.inSuburbs(toLoc)) return;

        //liquid can't flow from outside of city into the city
        if (toLocInCity && !fromLocInCity) {
            event.setCancelled(true);
            return;
        }

        ClaimEntry toClaim = SQLConn.getClaim(toLoc);
        if (!toLocInCity && toClaim == null) return;
        ClaimEntry fromClaim = SQLConn.getClaim(fromLoc);

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
        if (fromClaim != null && toClaim != null && !fromClaim.owner.equals(toClaim.owner)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        var damaged = (Player) event.getEntity();
        event.setCancelled(controller.inCity(damaged.getLocation()) && event.getDamager() instanceof Player);
    }

    //TODO make it so people can't open fence gates, click on buttons, press levers, etc in the city
    @EventHandler
    public void claimClicky(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) {
            return;
        }
        Location newLoc = e.getClickedBlock().getLocation();

        if (!controller.inSuburbs(newLoc)) return;

        //if player is clicking on a locked chest/door then don't let em
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK 
            && (Arrays.asList(nonClickables).contains(e.getClickedBlock().getType()) 
                || e.getClickedBlock().getState() instanceof Door)) {

            if (!controller.canBreak(e.getPlayer(), newLoc)) {
                e.getPlayer().sendMessage(ChatColor.RED + "woah that is locked");
                e.setCancelled(true);
            }
        }
        //if a player right clicks w/ the claim material then maybe claim some stuff!!
        else if (e.getPlayer().getInventory().getItemInMainHand().getType().equals(claimMaterial) && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Location lastLoc = players.remove(e.getPlayer());
            if (lastLoc != null) {
                //you can't claim the block you already clicked, silly
                if (newLoc.getBlockX() == lastLoc.getBlockX() && newLoc.getBlockZ() == lastLoc.getBlockZ()) {
                    e.getPlayer().sendMessage(ChatColor.RED + "try claiming more than 1 block");

                } else {
                    controller.tryClaimLand(e.getPlayer(), lastLoc.getBlockX(), lastLoc.getBlockZ(), newLoc.getBlockX(), newLoc.getBlockZ());
                }
            } else {
                players.put(e.getPlayer(), e.getClickedBlock().getLocation());
                e.getPlayer().sendMessage("claim started @ (" + newLoc.getBlockX() + ", " + newLoc.getBlockZ() + ").");

                if (claimTimeoutMS != 0)
                    claimTimer.schedule(new PlayerUnclaimTimer(e.getPlayer(), newLoc), claimTimeoutMS);
            }
        }
    }

    //funny
    class PlayerUnclaimTimer extends TimerTask { //timer already cancelled error? what
        private final Player removePlayer;
        private final Location removeLocation;

        PlayerUnclaimTimer(Player removePlayer, Location removeLocation) {
            this.removePlayer = removePlayer;
            this.removeLocation = removeLocation;
        }

        @Override
        public void run() {
            try {
                Location l = players.get(removePlayer);
                if (l != null && !l.equals(removeLocation))
                    return;
                players.remove(removePlayer);
                removePlayer.sendMessage(ChatColor.RED + "claim @ (" + removeLocation.getBlockX() + ", " + removeLocation.getBlockZ() + ") timed out");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


