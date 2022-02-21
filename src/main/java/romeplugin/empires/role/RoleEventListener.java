/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package romeplugin.empires.role;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import romeplugin.election.PartyHandler;

public class RoleEventListener implements Listener {
    private final RoleHandler roleHandler;
    private final PartyHandler partyHandler;

    public RoleEventListener(RoleHandler roleHandler, PartyHandler partyHandler) {
        this.roleHandler = roleHandler;
        this.partyHandler = partyHandler;
    }

    @EventHandler
    public void handlePlayerChat(AsyncPlayerChatEvent event) {
        var role = roleHandler.getPlayerRole(event.getPlayer());
        if (role != null) {
            // intercept chat message here
            var party = partyHandler.getParty(event.getPlayer().getUniqueId());
            var partyTitle = party != null ? "(" + party.color + party.acronym + ChatColor.RESET + ") " : "";
            event.setFormat("[" + role.color + role.name + ChatColor.RESET + "] " + partyTitle + event.getFormat());
        }
    }

    /*
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
            var stmt = SQLConn.getConnection().prepareStatement("DELETE FROM titles WHERE uuid = ? AND title = 'POPE';");
            stmt.setString(1, event.getEntity().getUniqueId().toString());
            if (stmt.executeUpdate() > 0) {
                RomePlugin.onlinePlayerTitles.remove(event.getEntity());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }*/
}
