package romeplugin.messageIntercepter;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class DistanceListener implements Listener {
    private final int distance;
    private final boolean doSwearFilter;

    public DistanceListener(int distance, boolean doSwearFilter) {
        this.distance = distance;
        this.doSwearFilter = doSwearFilter;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMessage(AsyncPlayerChatEvent e) {
        String message = e.getMessage();
        if (doSwearFilter)
            message = SwearFilter.replaceSwears(message);
        e.setMessage(message);
        if (distance == 0)
            e.getRecipients().removeIf(p -> p.getLocation().distance(e.getPlayer().getLocation()) > distance);
    }
}
