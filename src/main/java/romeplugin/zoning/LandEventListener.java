package romeplugin.zoning;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import romeplugin.database.ClaimEntry;
import romeplugin.database.SQLConn;
import romeplugin.zoning.locks.LockManager;

import java.util.*;

public class LandEventListener implements Listener {
    private final LockManager lockManager;
    private final LandControl controller;
    public static final Material DEFAULT_MATERIAL = Material.BRICK;

    private final Material claimMaterial;
    private final long claimTimeoutMS;

    private final Timer claimTimer = new Timer();

    //Hashmap of players and their claim start location to allow for claiming with a claim item (default: DEFAULT_MATERIAL)
    private final HashMap<Player, Location> players = new HashMap<>();

    //the materials people can't right-click in a claim/the city
    private final List<Material> nonClickables;

    //note last player who places sponge to make sure they arent tryna destroy water in someone else's claim
    //there will be a bug if someone who can build in the city places a sponge and then water flows from the city into the suburbs and someone already has
    //a sponge placed, it will get rid of the water (but who cares)
    private Player spongePlacer = null;

    public LandEventListener(
            LandControl controller,
            LockManager lockManager,
            Material claimMaterial,
            List<Material> autoLockedBlocks,
            long claimTimeoutMS
    ) {
        this.controller = controller;
        this.lockManager = lockManager;
        this.claimMaterial = claimMaterial;
        this.nonClickables = autoLockedBlocks;
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
        // TODO: if the block successfully breaks, remove the lock on it
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        var block = event.getBlock();
        if (event.getBlock().getType().equals(Material.SPONGE)) this.spongePlacer = event.getPlayer();
        if (!controller.inSuburbs(block.getLocation())) return;
        if (controller.canBreak(event.getPlayer(), block.getLocation()))
            return;

        event.getPlayer().sendMessage(ChatColor.RED + "you can't build here");
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
        e.blockList().removeIf(block -> controller.inCity(block.getLocation()) || SQLConn.getClaim(block.getLocation()) != null);

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

    //Tests to see if a block can move from one block to another block (like flowing water, pistons, or dispensers)
    private boolean canBlockMove(Location fromLoc, Location toLoc) {
        // fast path, the wilderness has no protections
        if (controller.inWilderness(toLoc)) {
            return false;
        }

        boolean toLocInCity = controller.inCity(toLoc);
        boolean fromLocInCity = controller.inCity(fromLoc);

        if (toLocInCity && !fromLocInCity) {
            return false;
        }

        ClaimEntry toClaim = SQLConn.getClaim(toLoc);
        if (!toLocInCity && toClaim == null) {
            return true;
        }
        ClaimEntry fromClaim = SQLConn.getClaim(fromLoc);

        return toClaim == null || // can move into unclaimed land
                (fromClaim != null && fromClaim.owner.equals(toClaim.owner)); // can move between owned claims
    }

    @EventHandler
    public void onPistonPush(BlockPistonExtendEvent e) {
        var initLoc = e.getBlock().getLocation();
        var toLoc = e.getBlock().getRelative(((Directional) e.getBlock().getBlockData()).getFacing()).getLocation();
        if (!canBlockMove(initLoc, toLoc)) {
            e.setCancelled(true);
            return;
        }
        for (Block b : e.getBlocks()) {
            if (!canBlockMove(initLoc, b.getLocation())) {
                e.setCancelled(true);
                return;
            }
        }
    }

    //When a dispenser outside the city tries to dispense something into the city, then cancel it
    @EventHandler
    public void blockDispensedEvent(BlockDispenseEvent e) {
        Block fromBlock = e.getBlock();
        if (!(fromBlock.getType().equals(Material.DISPENSER))) return;

        Dispenser dispenser = (Dispenser) fromBlock.getState();
        Block toBlock = fromBlock.getRelative(((Directional) dispenser.getBlockData()).getFacing());

        e.setCancelled(!canBlockMove(fromBlock.getLocation(), toBlock.getLocation()));
    }

    //Only applies to water and lava
    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (event.getBlock().getType() == Material.DRAGON_EGG) return;

        Location fromLoc = event.getBlock().getLocation();
        Location toLoc = event.getToBlock().getLocation();

        event.setCancelled(!canBlockMove(fromLoc, toLoc));
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
        if (this.spongePlacer == null) return;

        e.getBlocks().removeIf(block -> !controller.canBreak(spongePlacer, block.getLocation()));
        this.spongePlacer = null;
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

    @EventHandler
    public void onItemFrameObjectAdded(PlayerInteractEntityEvent e) {
        Location loc = e.getRightClicked().getLocation();
        if (!controller.inSuburbs(loc))
            return;
        var entity = e.getRightClicked();
        if ((entity instanceof ItemFrame || entity instanceof ArmorStand) && !controller.canBreak(e.getPlayer(), loc)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
            var projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player &&
                    !(projectile instanceof Snowball) &&
                    !(projectile instanceof Egg)) {
                event.setCancelled(controller.inCity(event.getEntity().getLocation()));
            }
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

        Material mainHandItem = e.getPlayer().getInventory().getItemInMainHand().getType();

        //don't let players place/interact w/ armor stands in a claim
        if (mainHandItem.equals(Material.ARMOR_STAND)) {
            e.setCancelled(true);
            return;
        }

        //if player is clicking on a locked chest/door then don't let em
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK
                && e.getHand() != EquipmentSlot.OFF_HAND
                && (nonClickables.contains(e.getClickedBlock().getType())
                || e.getClickedBlock().getState() instanceof Door)) {

            if (e.getPlayer().getGameMode() == GameMode.SPECTATOR) return;
            var maybeLocked = lockManager.getBlockLockId(e.getClickedBlock());
            if (maybeLocked.isPresent()) {
                if (e.getItem() != null) {
                    var maybeKey = lockManager.getKey(e.getItem());
                    if (maybeKey.equals(maybeLocked)) {
                        e.setCancelled(false);
                        return;
                    }
                }
                // check if the lock's owner is the player who is trying to open it
                maybeLocked.ifPresent(id -> {
                    var owner = lockManager.getOwner(id);
                    e.setCancelled(!owner.equals(e.getPlayer().getUniqueId()));
                });
                return;
            }
            //var lockOwner = SQLConn.getLockOwner(e.getClickedBlock()); //TODO this
            //if (lockOwner != null && lockOwner.equals(e.getPlayer().getUniqueId());

            if (!controller.canBreak(e.getPlayer(), newLoc)) {
                e.getPlayer().sendMessage(ChatColor.RED + "woah that is locked");
                e.setCancelled(true);
            }
        }
        //if a player right clicks w/ the claim material then maybe claim some stuff!!
        else if (mainHandItem.equals(claimMaterial) && e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getHand() != EquipmentSlot.OFF_HAND) {
            Location lastLoc = players.remove(e.getPlayer());
            if (lastLoc != null) {
                //you can't claim the block you already clicked, silly
                if (newLoc.getBlockX() == lastLoc.getBlockX() && newLoc.getBlockZ() == lastLoc.getBlockZ()) {
                    e.getPlayer().sendMessage(ChatColor.RED + "try claiming more than 1 block");
                    return;

                } else {
                    controller.tryClaimLand(e.getPlayer(), lastLoc.getBlockX(), lastLoc.getBlockZ(), newLoc.getBlockX(), newLoc.getBlockZ());
                    return;
                }
            } else {
                players.put(e.getPlayer(), e.getClickedBlock().getLocation());
                e.getPlayer().sendMessage("claim started @ (" + newLoc.getBlockX() + ", " + newLoc.getBlockZ() + ").");

                //too cursed
                //if (claimTimeoutMS != 0)
                // claimTimer.schedule(new PlayerUnclaimTimer(e.getPlayer(), newLoc), claimTimeoutMS);
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


