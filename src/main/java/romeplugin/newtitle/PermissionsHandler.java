package romeplugin.newtitle;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import romeplugin.database.TitleEntry;

import java.util.HashMap;
import java.util.UUID;

public class PermissionsHandler {
    private final Plugin plugin;
    private final HashMap<UUID, PermissionAttachment> perms = new HashMap<>();

    public PermissionsHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    public void playerJoin(Player player, TitleEntry title) {
        var attachment = player.addAttachment(plugin);
        perms.put(player.getUniqueId(), attachment);
        if (title != null && title.t.perms != null) {
            attachment.setPermission(title.t.perms, true);
        }
    }

    public void deleteTitle(Player player, Title oldTitle) {
        var playerPerm = perms.get(player.getUniqueId());
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
