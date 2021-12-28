package romeplugin.messageIntercepter;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import romeplugin.RomePlugin;

import romeplugin.newtitle.Title;

public class ShoutCommand implements CommandExecutor  {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        Title playerTitle = RomePlugin.onlinePlayerTitles.get(player);
        if (playerTitle == null) return false;
        String message = "";
        if (args.length == 0) 
            message = "aaaaah";
        else 
            message = String.join(" ", args);

        message = "[" + playerTitle.color + playerTitle.fancyName + ChatColor.RESET + "] " + message;
        
        for (var p: sender.getServer().getOnlinePlayers()) {
            p.sendMessage("[" + ChatColor.RED + "SHOUT" + ChatColor.RESET + "] " + message);
        }
        return true;
    }
}
