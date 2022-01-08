package romeplugin.title;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.UUID;

class PermissionsHandler {
    private final Plugin plugin;
    private final HashMap<UUID, PermissionAttachment> perms = new HashMap<>();

    public PermissionsHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    public void playerJoin(Player player, Title title) {
        var attachment = player.addAttachment(plugin);
        perms.put(player.getUniqueId(), attachment);
        if (title != null && title.perms != null) {
            attachment.setPermission(title.perms, true);
        }
    }

    public void deleteTitle(UUID uuid, Title oldTitle) {
        var playerPerm = perms.get(uuid);
        if (playerPerm == null) {
            return;
        }
        if (oldTitle.perms != null) {
            playerPerm.setPermission(oldTitle.perms, false);
        }
    }

    public void updateTitle(Player player, Title newTitle, Title oldTitle) {
        var playerPerm = perms.get(player.getUniqueId());
        if (oldTitle != null && oldTitle.perms != null) {
            playerPerm.setPermission(oldTitle.perms, false);
        }
        if (newTitle.perms != null) {
            playerPerm.setPermission(newTitle.perms, true);
        }
    }

    public void playerQuit(Player player) {
        perms.get(player.getUniqueId()).remove();
    }
}
