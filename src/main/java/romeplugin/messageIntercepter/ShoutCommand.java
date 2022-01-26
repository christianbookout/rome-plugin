package romeplugin.messageIntercepter;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import romeplugin.RomePlugin;
import romeplugin.election.PartyHandler;
import romeplugin.title.Title;

import java.util.Collections;
import java.util.List;

public class ShoutCommand implements CommandExecutor, TabCompleter {
    private final PartyHandler partyHandler;
    public ShoutCommand(PartyHandler partyHandler) {
        this.partyHandler = partyHandler;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        String title = "";
        var partyTitle = "";
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Title playerTitle = RomePlugin.onlinePlayerTitles.get(player);
            var party = partyHandler.getParty(player.getUniqueId());
            partyTitle = party != null ? "(" + party.color + party.acronym + ChatColor.RESET + ") " : "";
            if (playerTitle != null)
                title = "[" + playerTitle.color + playerTitle.fancyName + ChatColor.RESET + "] ";
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
