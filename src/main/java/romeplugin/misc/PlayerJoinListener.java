package romeplugin.misc;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import romeplugin.database.SQLConn;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        SQLConn.setUsername(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }
}
