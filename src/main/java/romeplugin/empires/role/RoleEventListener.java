/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package romeplugin.empires.role;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
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

    @EventHandler
    public void onDeathEvent(PlayerDeathEvent event) {
        var role = roleHandler.getPlayerRole(event.getEntity());
        if (role != null && role.hasPerm(Permission.RemoveOnDeath)) {
            roleHandler.removePlayerRole(event.getEntity());
        }
    }
}
