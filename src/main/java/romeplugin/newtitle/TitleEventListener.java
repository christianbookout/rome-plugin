/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package romeplugin.newtitle;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import romeplugin.RomePlugin;
import romeplugin.database.SQLConn;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author chris
 */
public class TitleEventListener implements Listener {
    private final Plugin plugin;
    private final HashMap<UUID, PermissionAttachment> perms = new HashMap<>();

    public TitleEventListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handlePlayerChat(AsyncPlayerChatEvent event) {
        Title playerTitle = RomePlugin.onlinePlayerTitles.get(event.getPlayer());
        if (playerTitle != null) {
            // intercept chat message here
            event.setFormat("[" + playerTitle.color + playerTitle.fancyName + ChatColor.RESET + "] " + event.getFormat());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        var title = SQLConn.getTitle(player.getUniqueId());
        var playerPerm = player.addAttachment(plugin);
        perms.put(player.getUniqueId(), playerPerm);
        SQLConn.setUsername(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        if (title == null) {
            return;
        }
        if (title.t.perms != null) {
            playerPerm.setPermission(title.t.perms, true);
        }
        RomePlugin.onlinePlayerTitles.put(event.getPlayer(), title.t);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.getPlayer().removeAttachment(perms.remove(event.getPlayer().getUniqueId()));
        RomePlugin.onlinePlayerTitles.remove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        try {
            var stmt = SQLConn.getConnection().prepareStatement("DELETE FROM players WHERE uuid = ? AND title = 'POPE';");
            stmt.setString(1, event.getEntity().getUniqueId().toString());
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
