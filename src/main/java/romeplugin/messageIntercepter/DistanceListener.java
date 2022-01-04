package romeplugin.messageIntercepter;

import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import romeplugin.zoning.claims.LandControl;

public class DistanceListener implements Listener {
    private final int distance;
    private final SwearFilter swearFilter;
    private final LandControl controller;

    public DistanceListener(int distance, SwearFilter swearFilter, LandControl controller) {
        this.distance = distance;
        this.swearFilter = swearFilter;
        this.controller = controller;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMessage(AsyncPlayerChatEvent e) {
        String message = e.getMessage();
        if (swearFilter.doFilter(e.getPlayer())) {
            message = swearFilter.replaceSwears(message);
        }
        e.setMessage(message);
        Environment playerEnvironment = e.getPlayer().getLocation().getWorld().getEnvironment();
        if (this.distance != 0) {
            boolean playerInCity = controller.inCity(e.getPlayer().getLocation());
            e.getRecipients().removeIf(
                p -> 
                !(playerInCity && controller.inCity(p.getLocation()))
                && (!playerEnvironment.equals(p.getLocation().getWorld().getEnvironment())
                    || p.getLocation().distance(e.getPlayer().getLocation()) > this.distance) 
            );
        }
    }
}
