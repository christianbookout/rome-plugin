package romeplugin.zoning;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import romeplugin.zoning.claims.LandControl;

public class LandEnterListener implements Listener {
    private final LandControl controller;

    public LandEnterListener(LandControl controller) {
        this.controller = controller;
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent e) {
        if (e.getTo() == null) {
            return;
        }
        if (e.getTo().getBlockX() == e.getFrom().getBlockX() && e.getTo().getBlockZ() == e.getFrom().getBlockZ())
            return;

        CityArea newZone = controller.getArea(e.getTo());
        CityArea oldZone = controller.getArea(e.getFrom());
        if (oldZone != null && newZone == null) {
            e.getPlayer().sendMessage("you've entered the " + ChatColor.DARK_GREEN + "wilderness");
            return;
        }
        if (newZone != null && (oldZone == null || oldZone.getType() != newZone.getType())) {
            e.getPlayer().sendMessage("you've entered the " +
                    newZone.getType().color + newZone.getType().toString().toLowerCase() + " zone");
        }
    }
}
