package romeplugin.zoning;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import romeplugin.zoning.claims.LandControl;

public class LandCommand implements CommandExecutor {
    private final LandControl controller;

    public LandCommand(LandControl controller) {
        this.controller = controller;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("/rome found [size] -- sina pali lon sina e Wome");
            sender.sendMessage("/rome expand <size> -- accepts negative values");
            return false;
        }
        switch (args[0]) {
            case "found":
                sender.sendMessage("sina nasa, ni li NYI...");
                return found();
            case "expand":
                if (!controller.expandGovernment(Integer.parseInt(args[1]))) {
                    sender.sendMessage("size would make the government's size negative");
                    return false;
                }
                return true;
        }
        return false;
    }

    private boolean found() {
        // TODO: reimplement this command
        return false;
    }
}
