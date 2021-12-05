package romeplugin.zoning;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FoundCityCommand implements CommandExecutor {
    private final LandControl control;

    public FoundCityCommand(LandControl control) {
        this.control = control;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] params) {
        var p = (Player) commandSender;
        if (p == null) {
            return false;
        }
        var loc = p.getLocation();
        control.setCenter(loc.getBlockX(), loc.getBlockY());
        control.setGovernmentSize(50);
        return true;
    }
}
