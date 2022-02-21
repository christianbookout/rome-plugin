package romeplugin.zoning;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CityCommand implements CommandExecutor {
    private final CityManager manager;

    public CityCommand(CityManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // TODO: add disbanding a city
        if (args.length == 0) {
            sender.sendMessage("/rome found [size] -- sina pali lon sina e Wome");
            sender.sendMessage("/rome expand <name> <size> -- accepts negative values");
            return false;
        }
        switch (args[0]) {
            case "found":
                if (args.length < 2) {
                    sender.sendMessage("no city name :(");
                    return true;
                }
                if (sender instanceof Player) {
                    manager.foundCity((Player) sender, args[1]);
                    return true;
                }
                return false;
            case "expand":
                if (args.length < 3) {
                    sender.sendMessage("/rome expand <name> <size>");
                    return true;
                }
                manager.expandGovernment(args[1], Integer.parseInt(args[2]));
                return true;
        }
        return false;
    }
}
