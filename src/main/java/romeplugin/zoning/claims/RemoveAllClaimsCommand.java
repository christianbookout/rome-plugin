package romeplugin.zoning.claims;

import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;

public class RemoveAllClaimsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        UUID target = player.getUniqueId();
        //if the target is another player check if they're op
        if (args.length == 1) {
            UUID uuid = SQLConn.getUUIDFromUsername(args[0]);
            if (uuid == null) {
                player.sendMessage(MessageConstants.CANT_FIND_PLAYER);
                return true;
            }
            //if the player isn't using the command on themselves
            if (!uuid.equals(player.getUniqueId())) {
                //if they don't have permission to use the command on someone else
                if (!player.isOp()) {
                    player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
                    return true;
                } else {
                    target = uuid;
                }
            } 
        }

        try {
            SQLConn.getConnection().prepareStatement("DELETE FROM cityClaims WHERE owner_uuid = '" + target.toString() + "'");
            player.sendMessage("Successfully deleted all of " + args.toString() + "'s claims");
        } catch (SQLException e) {
            
        }

        return false;
    }
    
}
