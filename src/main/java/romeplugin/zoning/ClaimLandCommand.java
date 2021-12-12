package romeplugin.zoning;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import romeplugin.database.SQLConn;

public class ClaimLandCommand implements CommandExecutor {
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
            return claimLand(player, x0, y0, x1, y1);
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
        // ensure x0, y0 is the top left point and x1, y1 is the bottom right point
        var x0 = Math.min(xa, xb);
        var y0 = Math.max(ya, yb);
        var x1 = Math.max(xa, xb);
        var y1 = Math.min(ya, yb);
        return claimLand(p, x0, y0, x1, y1);
    }

    public static boolean claimLand(Player player, int x0, int y0, int x1, int y1) {
        var claim = SQLConn.getClaimRect(x0, y0, x1, y1);
        if (claim != null) {
            player.sendMessage("land already claimed >:(");
            return false;
        }
        SQLConn.addClaim(x0, y0, x1, y1, player.getUniqueId());
        player.sendMessage("successfully claimed " + Math.abs((x1 - x0) * (y0 - y1)) + " blocks.");
        return true;
    }
}
