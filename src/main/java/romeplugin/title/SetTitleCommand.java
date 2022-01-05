package romeplugin.title;

import org.bukkit.ChatColor;
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
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length < 2) {
            return false;
        }
        Title title = Title.getTitle(args[1]);
        if (title == null) {
            sender.sendMessage(ChatColor.RED + "invalid title");
            return true;
        }
        Player target = sender.getServer().getPlayer(args[0]);
        if (target == null) {
            var uuid = SQLConn.getUUIDFromUsername(args[0]);
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "ni jan li lon ala");
                return true;
            }
            titles.setTitleOffline(uuid, title);
            sender.sendMessage(args[0] + "'s title is now " + title.fancyName);
            return true;
        }

        if (!titles.setTitle(target, title)) {
            sender.sendMessage("could not set title??");
            return true;
        }

        sender.sendMessage(target.getDisplayName() + "'s title is now " + title.fancyName);
        return true;
    }
}
