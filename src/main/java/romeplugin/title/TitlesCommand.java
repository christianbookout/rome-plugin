package romeplugin.title;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TitlesCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        String message = "Rome Titles: ";
        var titles = Arrays.stream(Title.values())
                .map(t -> t.color + t.fancyName)
                .collect(Collectors.joining(ChatColor.RESET + ", "));
        sender.sendMessage(message + titles);
        return true;
    }
}
