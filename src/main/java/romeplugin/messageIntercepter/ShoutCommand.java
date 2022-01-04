package romeplugin.messageIntercepter;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import romeplugin.RomePlugin;
import romeplugin.title.Title;

public class ShoutCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        String title = "";
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Title playerTitle = RomePlugin.onlinePlayerTitles.get(player);
            if (playerTitle != null)
                title = "[" + playerTitle.color + playerTitle.fancyName + ChatColor.RESET + "] ";
        }

        String message;
        if (args.length == 0)
            message = "aaaaah";
        else
            message = String.join(" ", args);

        message = title + "<" + sender.getName() + "> " + message;
        sender.getServer().broadcastMessage("[" + ChatColor.RED + "SHOUT" + ChatColor.RESET + "] " + message);
        return true;
    }
}
