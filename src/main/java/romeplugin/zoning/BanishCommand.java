package romeplugin.zoning;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;
import romeplugin.empires.role.Permission;
import romeplugin.empires.role.RoleHandler;

public class BanishCommand implements CommandExecutor {
    private final LandEnterListener landEnter;
    private final RoleHandler roleHandler;

    public BanishCommand(LandEnterListener landEnter, RoleHandler roleHandler) {
        this.landEnter = landEnter;
        this.roleHandler = roleHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            return false;
        }
        boolean hasPerm = false;
        if (sender instanceof Player) {
            hasPerm = roleHandler.getPlayerRole((Player) sender).hasPerm(Permission.CanBanish);
        }
        if (!sender.isOp() || !hasPerm) {
            sender.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
            return true;
        }

        int banishedCount = 0;
        for (var playerName : args) {
            var uuid = SQLConn.getUUIDFromUsername(playerName);
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "could not find player '" + playerName + "'");
                continue;
            }
            ++banishedCount;
            landEnter.banish(uuid);
        }
        if (banishedCount == 0) {
            sender.sendMessage("banished nobody... :(");
        } else if (banishedCount == 1) {
            sender.sendMessage("banished " + args[0]);
        } else {
            sender.sendMessage("banished " + banishedCount + " players");
        }
        return true;
    }
}
