/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package romeplugin.newtitle;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import romeplugin.RomePlugin;
import romeplugin.database.SQLConn;

/**
 * @author chris
 */
public class TitleEventListener implements Listener {
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
        var title = SQLConn.getTitle(event.getPlayer().getUniqueId());
        SQLConn.setUsername(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        if (title == null) {
            return;
        }
        RomePlugin.onlinePlayerTitles.put(event.getPlayer(), title.t);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        RomePlugin.onlinePlayerTitles.remove(event.getPlayer());
    }
}
