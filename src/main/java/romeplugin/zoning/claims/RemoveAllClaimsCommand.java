package romeplugin.zoning.claims;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;

import java.sql.SQLException;
import java.util.UUID;

public class RemoveAllClaimsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        UUID target;
        String targetName;
        //if the target is another player check if they're op
        if (args.length == 1) {
            target = SQLConn.getUUIDFromUsername(args[0]);
            if (target == null) {
                sender.sendMessage(MessageConstants.CANT_FIND_PLAYER);
                return true;
            }
            //if they don't have permission to use the command on someone else
            if (!sender.isOp()) {
                sender.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
                return true;
            }
            targetName = args[0];
        } else if (sender instanceof Player) {
            Player player = (Player) sender;
            target = player.getUniqueId();
            targetName = player.getName();
        } else {
            return false;
        }

        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("DELETE FROM cityClaims WHERE owner_uuid = ?;");
            stmt.setString(1, target.toString());
            stmt.execute();
            stmt.close();
            sender.sendMessage("Successfully deleted all of " + targetName + "'s claims");
        } catch (SQLException e) {
            sender.sendMessage("oopsies! we're vewwy sowwy!! o(╥﹏╥)o something went wrong...");
            e.printStackTrace();
        }
        return true;
    }

}
