package romeplugin.zoning.locks;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import romeplugin.database.SQLConn;

import java.sql.SQLException;
import java.util.OptionalInt;
import java.util.UUID;

public class LockManager {
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

    public OptionalInt lockId(Block block) {
        try {
            var stmt = SQLConn.getConnection()
                    .prepareStatement("SELECT * FROM lockedBlocks WHERE x = ? AND y = ? AND z = ?");
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

    public OptionalInt tryCreateKey(UUID uuid) {
        try {
            var stmt = SQLConn.getConnection().prepareStatement("INSERT INTO lockKeys (creator_uuid) VALUES (?);");
            stmt.setString(1, uuid.toString());
            var res = stmt.executeQuery();
            if (!res.next()) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(res.getInt("keyId"));
        } catch (SQLException e) {
            e.printStackTrace();
            return OptionalInt.empty();
        }
    }
}
