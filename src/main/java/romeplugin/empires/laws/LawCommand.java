package romeplugin.empires.laws;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class LawCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;
        if (args.length <= 0) {
            help(player);
            return true;
        }
        switch (args[0]) {
            case "help":
                help(player);
                return true;
            case "propose":
                if (args.length <= 2) return false;
                return true;
            case "delete":
                if (args.length <= 1) return false;
                return true;

        }
        return false;
    }

    private void help(Player player) {

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
