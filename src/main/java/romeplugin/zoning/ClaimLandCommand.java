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
        return claimLand(p, xa, ya, xb, yb);
    }

    public static boolean claimLand(Player player, int x0, int y0, int x1, int y1) {
        // ensure x0, y0 is the top left point and x1, y1 is the bottom right point
        var x00 = Math.min(x0, x1);
        var y00 = Math.max(y0, y1);
        var x01 = Math.max(x0, x1);
        var y01 = Math.min(y0, y1);

        var claim = SQLConn.getClaimRect(x00, y00, x01, y01);
        if (claim != null) {
            player.sendMessage("land already claimed >:(");
            return false;
        }
        SQLConn.addClaim(x00, y00, x01, y01, player.getUniqueId());
        player.sendMessage("successfully claimed " + (x01 - x00 + 1) * (y00 - y01 + 1) + " blocks.");
        return true;
    }
}
