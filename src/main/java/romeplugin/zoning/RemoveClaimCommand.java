package romeplugin.zoning;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import romeplugin.database.SQLConn;

public class RemoveClaimCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        var entity = (Entity)sender;
        if (entity == null) {
            return false;
        }
        var loc = entity.getLocation();
        var claim = SQLConn.getClaim(loc.getBlockX(), loc.getBlockZ());
        if (claim == null) {
            sender.sendMessage("no claim here");
            return false;
        }
        if (!SQLConn.removeClaim(claim)) {
            sender.sendMessage("database error!");
            return false;
        }
        sender.sendMessage("successfully removed claim");
        return true;
    }
}
