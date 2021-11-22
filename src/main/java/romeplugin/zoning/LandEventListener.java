package romeplugin.zoning;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class LandEventListener implements Listener {
    private final LandControl controller;

    public LandEventListener(LandControl controller) {
        this.controller = controller;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        var block = event.getBlock();
        event.setCancelled(!controller.canBreak(block.getX(), block.getY()));
    }
}
