package romeplugin.zoning;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import romeplugin.database.SQLConn;
import romeplugin.zoning.claims.LandControl;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LandEnterListener implements Listener {
    private final LandControl controller;
    private final Set<UUID> banished = new HashSet<>();

    public LandEnterListener(LandControl controller) {
        this.controller = controller;
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT * FROM banished;");
            var res = stmt.executeQuery();
            while (res.next()) {
                banished.add(UUID.fromString(res.getString("uuid")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent e) {
        if (e.getTo() == null) {
            return;
        }
        if (e.getTo().getBlockX() == e.getFrom().getBlockX() && e.getTo().getBlockZ() == e.getFrom().getBlockZ())
            return;

        CityArea newZone = controller.getArea(e.getTo());
        var isBanished = banished.contains(e.getPlayer().getUniqueId());
        if (newZone != null && isBanished) {
            e.setCancelled(true);
            return;
        }
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

    public void banish(UUID who) {
        banished.add(who);
        // update sql too!
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("REPLACE INTO banished VALUES ?;");
            stmt.setString(1, who.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
