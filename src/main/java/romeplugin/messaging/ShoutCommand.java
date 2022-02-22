package romeplugin.messaging;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import romeplugin.election.PartyHandler;
import romeplugin.empires.role.RoleHandler;

import java.util.Collections;
import java.util.List;

public class ShoutCommand implements CommandExecutor, TabCompleter {
    private final PartyHandler partyHandler;
    private final RoleHandler roleHandler;

    public ShoutCommand(PartyHandler partyHandler, RoleHandler roleHandler) {
        this.partyHandler = partyHandler;
        this.roleHandler = roleHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        String title = "";
        var partyTitle = "";
        if (sender instanceof Player) {
            Player player = (Player) sender;
            var role = roleHandler.getPlayerRole(player);
            var party = partyHandler.getParty(player.getUniqueId());
            partyTitle = party != null ? "(" + party.color + party.acronym + ChatColor.RESET + ") " : "";
            if (role != null)
                title = "[" + role.color + role.name + ChatColor.RESET + "] ";
        }

        String message;
        if (args.length == 0)
            message = "aaaaah";
        else
            message = String.join(" ", args);

        message = title + partyTitle + "<" + sender.getName() + "> " + message;
        sender.getServer().broadcastMessage("[" + ChatColor.RED + "SHOUT" + ChatColor.RESET + "] " + message);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return Collections.emptyList();
    }
}
