package romeplugin.empires.role;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;

public class RemoveRoleCommand implements CommandExecutor {
    private final RoleHandler roleHandler;

    public RemoveRoleCommand(RoleHandler roleHandler) {
        this.roleHandler = roleHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] params) {
        if (!sender.isOp()) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
                return true;
            }
            var role = roleHandler.getPlayerRole((Player) sender);
            if (role == null || !role.hasPerm(Permission.EditRoles)) {
                sender.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
                return true;
            }
        }
        if (params.length < 1) {
            sender.sendMessage(ChatColor.RED + "sina wile e jan!");
            return true;
        }
        var target = sender.getServer().getPlayer(params[0]);
        if (target == null) {
            var uuid = SQLConn.getUUIDFromUsername(params[0]);
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "ni jan li lon ala!");
                return true;
            }
            roleHandler.removePlayerRole(uuid);
        } else {
            roleHandler.removePlayerRole(target);
        }
        sender.sendMessage("weka e nimi wawa lon jan " + params[0] + ".");
        return true;
    }
}
