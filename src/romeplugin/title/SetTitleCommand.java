package romeplugin.title;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetTitleCommand implements CommandExecutor {
    private final RomeTitles titles;

    public SetTitleCommand(RomeTitles titles) {
        this.titles = titles;
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] params) {
        if (params.length > 1) {
            if (!titles.isTitle(params[1])) { return false; }
            Player target = commandSender.getServer().getPlayer(params[0]);
            if (target == null) { return false; }
            titles.setPlayerTitle(target.getUniqueId(), params[1]);
            return true;
        }
        return false;
    }
}
