package romeplugin.newtitle;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetTitleCommand implements CommandExecutor {
    private final TitleHandler titles;

    public SetTitleCommand(TitleHandler titles) {
        this.titles = titles;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] params) {
        if (params.length < 2) {
            return false;
        }
        Title title;
        try {
            title = Title.getTitle(params[1]);
        } catch (IllegalArgumentException e) {
            commandSender.sendMessage("invalid title :)");
            return false;
        }
        Player target = commandSender.getServer().getPlayer(params[0]);
        if (target == null) {
            return false;
        }

        if (!titles.setTitle(target, title)) {
            commandSender.sendMessage("could not set title??");
        }

        commandSender.sendMessage(target.getDisplayName() + "'s title is now " + title.toString());
        return true;
    }
}
