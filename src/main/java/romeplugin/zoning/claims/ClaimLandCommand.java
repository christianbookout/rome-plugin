package romeplugin.zoning.claims;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClaimLandCommand implements CommandExecutor {
    private final LandControl landControl;

    public ClaimLandCommand(LandControl landControl) {
        this.landControl = landControl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length < 2) {
            return false;
        }
        if (args[0].equals("radius")) {
            var player = (Player) sender;
            if (player == null) {
                return false;
            }
            var r = Integer.parseInt(args[1]);
            var loc = player.getLocation();
            var x0 = loc.getBlockX() - r;
            var y0 = loc.getBlockZ() + r;
            var x1 = loc.getBlockX() + r;
            var y1 = loc.getBlockZ() - r;
            return landControl.tryClaimLand(player, x0, y0, x1, y1);
        }
        if (args.length < 4) {
            return false;
        }
        var p = (Player) sender;
        if (p == null) {
            return false;
        }
        var xa = Integer.parseInt(args[0]);
        var ya = Integer.parseInt(args[1]);
        var xb = Integer.parseInt(args[2]);
        var yb = Integer.parseInt(args[3]);
        return landControl.tryClaimLand(p, xa, ya, xb, yb);
    }
}
