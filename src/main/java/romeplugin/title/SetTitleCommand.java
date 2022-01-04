package romeplugin.title;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import romeplugin.database.SQLConn;

public class SetTitleCommand implements CommandExecutor {
    private final TitleHandler titles;

    public SetTitleCommand(TitleHandler titles) {
        this.titles = titles;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length < 2) {
            return false;
        }
        Title title;
        try {
            title = Title.getTitle(args[1]);
        } catch (IllegalArgumentException e) {
            commandSender.sendMessage("invalid title :)");
            return false;
        }
        Player target = commandSender.getServer().getPlayer(args[0]);
        if (target == null) {
            var uuid = SQLConn.getUUIDFromUsername(args[0]);
            if (uuid == null) {
                commandSender.sendMessage("ni jan li lon ala");
                return false;
            }
            titles.setTitleOffline(uuid, title);
            commandSender.sendMessage(args[0] + "'s title is now " + title.fancyName);
            return true;
        }

        if (!titles.setTitle(target, title)) {
            commandSender.sendMessage("could not set title??");
        }

        commandSender.sendMessage(target.getDisplayName() + "'s title is now " + title.toString());
        return true;
    }
}
