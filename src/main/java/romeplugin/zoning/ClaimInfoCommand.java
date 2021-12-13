package romeplugin.zoning;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import romeplugin.database.SQLConn;

public class ClaimInfoCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        var entity = (Entity) sender;
        if (entity == null) {
            return false;
        }
        var loc = entity.getLocation();
        var claim = SQLConn.getClaim(loc.getBlockX(), loc.getBlockZ());
        if (claim == null) {
            sender.sendMessage("no claim here!");
            return true;
        }
        var owner_username = SQLConn.getUsername(claim.owner);
        if (owner_username == null) {
            owner_username = claim.owner.toString();
        }
        sender.sendMessage("claim owner: " + owner_username +
                "\nfrom (" + claim.x0 + ", " + claim.y0 + ") to (" + claim.x1 + ", " + claim.y1 + ")");
        return true;
    }
}
