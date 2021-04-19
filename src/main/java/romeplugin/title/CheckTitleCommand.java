package romeplugin.title;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CheckTitleCommand implements CommandExecutor {
    private final RomeTitles titles;

    public CheckTitleCommand(RomeTitles titles) {
        this.titles = titles;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] params) {
        commandSender.sendMessage("test'!");
        if (params.length > 0) {
            Player target = commandSender.getServer().getPlayer(params[0]);
            if (target == null) { return false; }
            Title n = titles.getPlayerTitle(target.getUniqueId());
            commandSender.sendMessage(target.getDisplayName() + " has title: " + n.name);
            return true;
        }
        return false;
    }
}
