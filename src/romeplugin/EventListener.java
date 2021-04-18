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
import romeplugin.title.RomeTitles;
import romeplugin.title.Title;

/**
 *
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
            event.setFormat("["+ playerTitle.color + playerTitle.name + ChatColor.WHITE + "] " + event.getFormat());
        }
    }
}
