package romeplugin.zoning;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;
import romeplugin.title.Title;

import java.util.Arrays;

public class BanishCommand implements CommandExecutor {
    private final LandEnterListener landEnter;

    private static final Title[] HAS_PERMISSIONS = new Title[]{
            Title.CONSUL,
            Title.CENSOR
    };

    public BanishCommand(LandEnterListener landEnter) {
        this.landEnter = landEnter;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            return false;
        }
        Title title = null;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            title = SQLConn.getTitle(player);
        }
        var titlesList = Arrays.asList(HAS_PERMISSIONS);
        boolean noPerms = title != null && !titlesList.contains(title);
        if (!sender.isOp() || noPerms) {
            sender.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
            return true;
        }

        int banishedCount = 0;
        for (var playerName : args) {
            var uuid = SQLConn.getUUIDFromUsername(playerName);
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "could not find player '" + playerName + "'");
                continue;
            }
            ++banishedCount;
            landEnter.banish(uuid);
        }
        if (banishedCount == 0) {
            sender.sendMessage("banished nobody... :(");
        } else if (banishedCount == 1) {
            sender.sendMessage("banished " + args[0]);
        } else {
            sender.sendMessage("banished " + banishedCount + " players");
        }
        return true;
    }
}
