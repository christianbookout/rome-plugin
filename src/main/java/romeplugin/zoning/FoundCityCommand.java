package romeplugin.zoning;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import romeplugin.zoning.claims.LandControl;

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
        var size = 10;
        if (params.length > 0) {
            try {
                size = Integer.parseInt(params[0]);
            } catch (NumberFormatException e) {
                commandSender.sendMessage("invalid size");
                return false;
            }
        }
        var loc = p.getLocation();
        control.setCenter(loc.getBlockX(), loc.getBlockZ());
        control.setGovernmentSize(size);
        control.updateDB();
        return true;
    }
}
