/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package romeplugin;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import romeplugin.database.SQLConn;
import romeplugin.newtitle.Title;

/**
 * @author chris
 */
public class EventListener implements Listener {
    @EventHandler
    public void handlePlayerChat(AsyncPlayerChatEvent event) {
        Title playerTitle = RomePlugin.onlinePlayers.get(event.getPlayer());
        if (playerTitle != null) {
            // intercept chat message here
            event.setFormat("[" + playerTitle.toString() + ChatColor.WHITE + "] " + event.getFormat());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var title = SQLConn.getTitle(event.getPlayer().getUniqueId());
        if (title == null) {
            return;
        }
        RomePlugin.onlinePlayers.put(event.getPlayer(), title.t);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        RomePlugin.onlinePlayers.remove(event.getPlayer());
    }
}
