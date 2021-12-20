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
import romeplugin.RomePlugin;
import romeplugin.database.SQLConn;

import java.sql.SQLException;

/**
 * @author chris
 */
public class TitleEventListener implements Listener {
    private final TitleHandler titles;

    public TitleEventListener(TitleHandler titles) {
        this.titles = titles;
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
        SQLConn.setUsername(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        titles.playerJoin(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        titles.playerQuit(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        try {
            var stmt = SQLConn.getConnection().prepareStatement("DELETE FROM players WHERE uuid = ? AND title = 'POPE';");
            stmt.setString(1, event.getEntity().getUniqueId().toString());
            if (stmt.executeUpdate() > 0) {
                RomePlugin.onlinePlayerTitles.remove(event.getEntity());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
