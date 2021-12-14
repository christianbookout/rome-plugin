package romeplugin.messageIntercepter;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class DistanceListener implements Listener {
    private final int distance;
    private final Server server;
    private final boolean doSwearFilter;
    
    public DistanceListener(Server server, int distance, boolean doSwearFilter) {
        this.server = server;
        this.distance = distance;
        this.doSwearFilter = doSwearFilter;
    }

    @EventHandler (priority=EventPriority.HIGHEST)
    public void onMessage(AsyncPlayerChatEvent e) {
        String message = e.getMessage();
        if (doSwearFilter) 
            message = SwearFilter.replaceSwears(message);
        
        broadcastMessage(e.getPlayer(), message);
        e.setCancelled(true);
    }

    public void broadcastMessage(Player sender, final String message) {
        server.getOnlinePlayers().forEach(
            p ->  {
                if (distance == 0 || p.getLocation().distance(sender.getLocation()) <= distance) 
                    p.sendMessage(sender.getUniqueId(), message);
            }
        );
    }
}
