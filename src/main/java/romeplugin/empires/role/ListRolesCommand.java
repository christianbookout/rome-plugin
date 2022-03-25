package romeplugin.empires.role;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import romeplugin.empires.EmpireHandler;

import java.util.stream.Collectors;

public class ListRolesCommand implements CommandExecutor {
    private final EmpireHandler empireHandler;
    private final RoleHandler roleHandler;

    public ListRolesCommand(EmpireHandler empireHandler, RoleHandler roleHandler) {
        this.empireHandler = empireHandler;
        this.roleHandler = roleHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("not part of empire");
            return true;
        }
        var empire = empireHandler.getEmpire(((Player) sender).getUniqueId());
        if (empire == null) {
            sender.sendMessage("not part of empire");
            return true;
        }
        var roles = roleHandler.getEmpireRoles(empire.id);
        String message = empire.name + " roles: ";
        if (roles.isEmpty()) {
            sender.sendMessage(message + "none!");
            return true;
        }
        var titles = roles.stream()
                .map(role -> role.color + role.name)
                .collect(Collectors.joining(ChatColor.RESET + ", "));
        sender.sendMessage(message + titles);
        return true;
    }
}
