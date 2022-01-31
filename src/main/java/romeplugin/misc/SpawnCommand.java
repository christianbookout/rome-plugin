package romeplugin.misc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import romeplugin.MessageConstants;
import romeplugin.zoning.claims.LandControl;

public class SpawnCommand implements CommandExecutor {
    private final LandControl landControl;

    public SpawnCommand(LandControl landControl) {
        this.landControl = landControl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        // TODO: some kind timer for teleporting
        var player = (Player) sender;
        var pos = player.getWorld().getHighestBlockAt(landControl.getCenterX(), landControl.getCenterY());
        player.teleport(pos.getLocation());
        player.sendMessage("splash!");
        return true;
    }
    
}
