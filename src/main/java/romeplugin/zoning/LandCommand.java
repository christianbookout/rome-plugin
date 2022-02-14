package romeplugin.zoning;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LandCommand implements CommandExecutor {
    private final CityManager manager;

    public LandCommand(CityManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // TODO: add disbanding a city
        if (args.length == 0) {
            sender.sendMessage("/rome found [size] -- sina pali lon sina e Wome");
            sender.sendMessage("/rome expand <size> -- accepts negative values");
            return false;
        }
        switch (args[0]) {
            case "found":
                if (sender instanceof Player) {
                    manager.foundCity((Player) sender);
                    return true;
                }
                return false;
            case "expand":
                // TODO: fix this
                /*
                if (!manager.expandGovernment(Integer.parseInt(args[1]))) {
                    sender.sendMessage("size would make the government's size negative");
                    return false;
                }
                 */
                sender.sendMessage("nyi :)");
                return true;
        }
        return false;
    }
}
