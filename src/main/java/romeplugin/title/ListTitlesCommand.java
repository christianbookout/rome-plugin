package romeplugin.title;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ListTitlesCommand implements CommandExecutor {
    private final RomeTitles titles;

    public ListTitlesCommand(RomeTitles titles) {
        this.titles = titles;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        sender.sendMessage("Titles: ");
        titles.getAllKnownTitles().forEach(title -> sender.sendMessage("- " + title.color + title.name + ChatColor.WHITE));
        return true;
    }
}
