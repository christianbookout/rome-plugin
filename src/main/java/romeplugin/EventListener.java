/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package romeplugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import romeplugin.database.SQLConn;
import romeplugin.title.RomeTitles;
import romeplugin.newtitle.Title;

/**
 * @author chris
 */
public class EventListener implements Listener {
    private final RomeTitles titles;

    public EventListener(RomeTitles titles) {
        this.titles = titles;
    }
    //@EventHandeler
    //public void doSomethingOnInteract(PlayerInteractEvent e) {}

    @EventHandler
    public void handlePlayerChat(AsyncPlayerChatEvent event) {
        Title playerTitle = titles.getPlayerTitle(event.getPlayer().getUniqueId());
        if (playerTitle != null) {
            // intercept chat message here
            event.setFormat("[" + playerTitle.color + playerTitle.name + ChatColor.WHITE + "] " + event.getFormat());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String sQLString = "SELECT * FROM players WHERE uuid = " + event.getPlayer().getUniqueId().toString();
        try (ResultSet results = SQLConn.read(sQLString)) {
            if (results == null) {
                //TODO store player in database
            }

            String title = results.getString("title");
            if (title == null) {
                System.err.println("can't create or find " + event.getPlayer().getName() + "'s title in the database :(");
            }

            RomePlugin.onlinePlayers.put(event.getPlayer(), Title.getTitle(title));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
