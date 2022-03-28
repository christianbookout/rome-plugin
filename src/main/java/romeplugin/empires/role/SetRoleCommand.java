package romeplugin.empires.role;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import romeplugin.database.SQLConn;
import romeplugin.empires.EmpireHandler;
import romeplugin.empires.role.RoleHandler;

import java.util.UUID;

public class SetRoleCommand implements CommandExecutor {
    private final RoleHandler roleHandler;
    private final EmpireHandler empireHandler;

    public SetRoleCommand(RoleHandler roleHandler, EmpireHandler empireHandler) {
        this.roleHandler = roleHandler;
        this.empireHandler = empireHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length < 2) {
            return false;
        }
        if (!(sender instanceof Player)) {
            // TODO: maybe allow some kind of "empire_name.role" role names
            return false;
        }
        var player = (Player) sender;
        var empire = empireHandler.getPlayerEmpireId(player.getUniqueId());
        if (empire.isEmpty()) {
            sender.sendMessage("not in empire");
            return true;
        }
        var role = roleHandler.getRoleByName(empire.getAsInt(), args[1]);
        if (role == null) {
            sender.sendMessage(ChatColor.RED + "invalid title");
            return true;
        }
        Player target = sender.getServer().getPlayer(args[0]);
        UUID target_uuid;
        if (target == null) {
            target_uuid = SQLConn.getUUIDFromUsername(args[0]);
            if (target_uuid == null) {
                sender.sendMessage(ChatColor.RED + "ni jan li lon ala");
                return true;
            }
        } else {
            target_uuid = target.getUniqueId();
        }
        roleHandler.setPlayerRole(target_uuid, role);
        sender.sendMessage(args[0] + "'s title is now " + role.name);
        return true;
    }
}
