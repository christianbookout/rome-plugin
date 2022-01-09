package romeplugin.zoning.locks;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
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
        try {
            var stmt = SQLConn.getConnection()
                    .prepareStatement("SELECT keyId FROM lockedBlocks WHERE x = ? AND y = ? AND z = ?;");
            stmt.setInt(1, block.getX());
            stmt.setInt(2, block.getY());
            stmt.setInt(3, block.getZ());
            var res = stmt.executeQuery();
            if (!res.next()) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(res.getInt("keyId"));
        } catch (SQLException e) {
            e.printStackTrace();
            return OptionalInt.of(-9999); // hopefully this key doesn't exist
        }
    }

    OptionalInt tryCreateKey(UUID uuid) {
        try {
            var stmt = SQLConn.getConnection().prepareStatement("INSERT INTO lockKeys (creator_uuid) VALUES (?);" +
                    "SELECT LAST_INSERT_ID();");
            stmt.setString(1, uuid.toString());
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
        try {
            var stmt = SQLConn.getConnection().prepareStatement("SELECT creator_uuid FROM lockKeys WHERE keyId = ?;");
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
        try {
            var stmt = SQLConn.getConnection()
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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        var item = event.getItem();
        if (item == null) {
            return;
        }
        var maybeKey = getKey(item);
        maybeKey.ifPresent(keyId -> {
            if (event.getPlayer().isSneaking()) {
                var block = event.getClickedBlock();
                if (block == null) {
                    return;
                }
                // TODO: remove the lock when the block is already locked
                if (getBlockLockId(block).isEmpty()) {
                    if (lockBlock(block, keyId)) {
                        event.getPlayer().sendMessage("locked your " + block);
                    } else {
                        event.getPlayer().sendMessage("something went wrong!");
                    }
                }
            }
        });
    }
}
