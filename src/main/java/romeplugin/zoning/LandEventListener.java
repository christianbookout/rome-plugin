package romeplugin.zoning;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class LandEventListener implements Listener {
    private final LandControl controller;

    public LandEventListener(LandControl controller) {
        this.controller = controller;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        var block = event.getBlock();
        if (controller.canBreak(event.getPlayer(), block.getX(), block.getY())) 
            return;

        event.getPlayer().sendMessage("you can't break that, dumbass");
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        var block = event.getBlock();
        if (controller.canBreak(event.getPlayer(), block.getX(), block.getY()))
            return;
        event.getPlayer().sendMessage("no :)");
        event.setCancelled(true);
    }
}
