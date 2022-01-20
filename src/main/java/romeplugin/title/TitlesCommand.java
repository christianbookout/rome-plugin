package romeplugin.title;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TitlesCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        String message = "Rome Titles: ";
        Collection<String> titles = Arrays.asList(Title.values()).stream().map(t -> t.color + t.fancyName).collect(Collectors.toList());
        sender.sendMessage(message + String.join(ChatColor.WHITE + ", ", titles));
        return true;
    }
}
