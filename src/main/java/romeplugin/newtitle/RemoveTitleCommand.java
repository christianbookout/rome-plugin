package romeplugin.newtitle;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import romeplugin.database.SQLConn;

public class RemoveTitleCommand implements CommandExecutor {
    private final TitleHandler titles;

    public RemoveTitleCommand(TitleHandler titles) {
        this.titles = titles;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] params) {
        if (params.length < 1) {
            sender.sendMessage(ChatColor.RED + "sina wile e jan!");
            return false;
        }
        Player target = sender.getServer().getPlayer(params[0]);
        if (target == null) {
            var uuid = SQLConn.getUUIDFromUsername(params[0]);
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "ni jan li lon ala!");
                return false;
            }
            return titles.removeTitleOffline(uuid);
        }
        if (!titles.removeTitle(target)) {
            return false;
        }
        sender.sendMessage("weka e nimi wawa lon jan " + params[0] + ".");
        return true;
    }
}
