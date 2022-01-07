package romeplugin.zoning.claims;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import romeplugin.database.SQLConn;

import java.util.UUID;

public class GetClaimBlocksCommand implements CommandExecutor {
    private final LandControl landControl;

    public GetClaimBlocksCommand(LandControl landControl) {
        this.landControl = landControl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        UUID uuid;
        if (args.length > 0) {
            if (!sender.hasPermission("romeplugin.aedile")) {
                sender.sendMessage(ChatColor.RED + "sina wawa ala a!");
                return true;
            }
            uuid = SQLConn.getUUIDFromUsername(args[0]);
            if (uuid == null) {
                sender.sendMessage("that person does not exist!");
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                return false;
            }
            uuid = ((Player) sender).getUniqueId();
        }
        sender.sendMessage("total claimed area: " + SQLConn.getTotalClaimedBlocks(uuid) +
                "\ntotal claimed area inside suburbs: " + landControl.getClaimedBlocksInSuburbs(uuid));
        return true;
    }
}
