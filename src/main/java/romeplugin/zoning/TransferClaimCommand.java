package romeplugin.zoning;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import romeplugin.database.SQLConn;

public class TransferClaimCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("who?");
            return false;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("not a featherless biped.");
            return false;
        }
        var player = (Player) sender;
        var loc = player.getLocation();
        var target = args[0];
        var targetUUID = SQLConn.getUUIDFromUsername(target);
        if (targetUUID == null) {
            sender.sendMessage("invalid username");
            return false;
        }
        var claim = SQLConn.getClaim(loc);
        if (claim == null) {
            sender.sendMessage("no claim here");
            return false;
        }
        if (!claim.owner.equals(player.getUniqueId())) {
            sender.sendMessage("not your claim");
            return false;
        }
        if (!SQLConn.updateClaimOwner(claim, targetUUID)) {
            sender.sendMessage("database error!");
            return false;
        }
        sender.sendMessage("transferred claim to " + target);
        return true;
    }
}
