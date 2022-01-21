package romeplugin.zoning.locks;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;

import java.sql.SQLException;
import java.util.OptionalInt;
import java.util.UUID;

public class LockManager implements Listener {
    private final NamespacedKey lockKey;

    public LockManager(Plugin plugin) {
        lockKey = new NamespacedKey(plugin, "key-id");
    }

    public OptionalInt getKey(ItemStack stack) {
        var meta = stack.getItemMeta();
        if (meta == null) {
            return OptionalInt.empty();
        }
        var container = meta.getPersistentDataContainer();
        var keyId = container.get(lockKey, PersistentDataType.INTEGER);
        if (keyId == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(keyId);
    }

    public OptionalInt getBlockLockId(Block block) {
        try (
                var conn = SQLConn.getConnection();
                var stmt = conn
                        .prepareStatement("SELECT keyId FROM lockedBlocks WHERE x = ? AND y = ? AND z = ?;")
        ) {
            stmt.setInt(1, block.getX());
            stmt.setInt(2, block.getY());
            stmt.setInt(3, block.getZ());
            try (var res = stmt.executeQuery()) {
                if (!res.next()) {
                    return OptionalInt.empty();
                }
                return OptionalInt.of(res.getInt("keyId"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return OptionalInt.of(-9999); // hopefully this key doesn't exist
        }
    }

    OptionalInt tryCreateKey(UUID uuid) {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("INSERT INTO lockKeys (creator_uuid) VALUES (?);");
            stmt.setString(1, uuid.toString());
            stmt.execute();
            stmt.close();
            stmt = conn.prepareStatement("SELECT LAST_INSERT_ID();");
            var res = stmt.executeQuery();
            if (!res.next()) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(res.getInt(1));
        } catch (SQLException e) {
            e.printStackTrace();
            return OptionalInt.empty();
        }
    }

    public UUID getOwner(int keyId) {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT creator_uuid FROM lockKeys WHERE keyId = ?;");
            stmt.setInt(1, keyId);
            var res = stmt.executeQuery();
            if (!res.next()) {
                return null;
            }
            return UUID.fromString(res.getString("creator_uuid"));
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean makeKey(Player player, ItemStack targetItem) {
        var maybeKey = tryCreateKey(player.getUniqueId());
        if (maybeKey.isEmpty()) {
            return false;
        }
        var meta = targetItem.getItemMeta();
        if (meta == null) {
            return false;
        }
        meta.getPersistentDataContainer().set(lockKey, PersistentDataType.INTEGER, maybeKey.getAsInt());
        targetItem.setItemMeta(meta);
        return true;
    }

    public boolean lockBlock(Block block, int keyId) {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn
                    .prepareStatement("INSERT INTO lockedBlocks VALUES (?, ?, ?, ?);");
            stmt.setInt(1, block.getX());
            stmt.setInt(2, block.getY());
            stmt.setInt(3, block.getZ());
            stmt.setInt(4, keyId);
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeLock(Block block) {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn
                    .prepareStatement("DELETE FROM lockedBlocks WHERE x = ? AND y = ? AND z = ?;");
            stmt.setInt(1, block.getX());
            stmt.setInt(2, block.getY());
            stmt.setInt(3, block.getZ());
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        var item = event.getItem();
        if (item == null) {
            return;
        }
        getKey(item).ifPresent(keyId -> {
            event.setCancelled(true);
            if (event.getPlayer().isSneaking()) {
                var block = event.getClickedBlock();
                if (block == null) {
                    return;
                }
                var claim = SQLConn.getClaim(block.getX(), block.getZ());
                if (claim == null || !claim.owner.equals(event.getPlayer().getUniqueId())) {
                    event.getPlayer().sendMessage("you can only lock blocks in your claim");
                    return;
                }
                var maybeLock = getBlockLockId(block);
                if (maybeLock.isEmpty()) {
                    MessageConstants.sendOnSuccess(
                            lockBlock(block, keyId),
                            event.getPlayer(),
                            "locked your " + block.getType().name()
                    );
                } else {
                    if (!maybeLock.equals(OptionalInt.of(keyId))) {
                        event.getPlayer().sendMessage("le do ckiku ku na ckiku le ti stela (this is locked with a different key)");
                        return;
                    }
                    MessageConstants.sendOnSuccess(
                            removeLock(block),
                            event.getPlayer(),
                            "unlocked the block"
                    );
                }
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent event) {
        removeLock(event.getBlock());
    }
}
