package romeplugin.empires.role;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import romeplugin.MessageConstants;

import java.util.List;

public class RoleCommand implements CommandExecutor, TabCompleter {
    private final RoleHandler roleHandler;

    public RoleCommand(RoleHandler roleHandler) {
        this.roleHandler = roleHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        var player = (Player) sender;
        if (!sender.isOp()) {
            var role = roleHandler.getPlayerRole(player);
            if (role == null || !role.hasPerm(Permission.EditRoles)) {
                player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
                return true;
            }
        }
        if (args[0].equals("addperm")) {
            if (args.length < 4) {
                return false;
            }
            addPermission(player, args[1], args[2]);
        } else if (args[0].equals("removeperm")) {
            if (args.length < 4) {
                return false;
            }
            removePermission(player, args[1], args[2]);
        }
        return true;
    }

    public void addPermission(Player player, String roleName, String permissionName) {
        Permission perm;
        try {
            perm = Permission.valueOf(permissionName);
        } catch (IllegalArgumentException e) {
            player.sendMessage("invalid permission");
            return;
        }
        var role = roleHandler.getRole(roleName);
        if (role == null) {
            player.sendMessage("role name no found");
            return;
        }
        player.sendMessage("ok");
        roleHandler.addPermission(role.id, perm);
    }

    public void removePermission(Player player, String roleName, String permissionName) {
        Permission perm;
        try {
            perm = Permission.valueOf(permissionName);
        } catch (IllegalArgumentException e) {
            player.sendMessage("invalid permission");
            return;
        }
        var role = roleHandler.getRole(roleName);
        if (role == null) {
            player.sendMessage("role name no found");
            return;
        }
        player.sendMessage("ok");
        roleHandler.removePermission(role.id, perm);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
