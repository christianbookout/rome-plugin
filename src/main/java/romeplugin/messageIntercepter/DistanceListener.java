package romeplugin.messageIntercepter;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class DistanceListener implements Listener {
    private final int distance;
    private final SwearFilter swearFilter;

    public DistanceListener(int distance, SwearFilter swearFilter) {
        this.distance = distance;
        this.swearFilter = swearFilter;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMessage(AsyncPlayerChatEvent e) {
        String message = e.getMessage();
        if (swearFilter.doFilter(e.getPlayer())) {
            message = swearFilter.replaceSwears(message);
        }
        e.setMessage(message);
        if (this.distance != 0)
            e.getRecipients().removeIf(
                p -> 
                p.getLocation().distance(e.getPlayer().getLocation()) > this.distance 
                || !p.getLocation().getWorld().getEnvironment().equals(e.getPlayer().getWorld().getEnvironment())
            );
    }
}
