package romeplugin.zoning;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;
import romeplugin.title.Title;

public class BanishCommand implements CommandExecutor {

    private static final Title[] HAS_PERMISSIONS = new Title[] {
        Title.CONSUL,
        Title.CENSOR
    };

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
        return false;
    }
    
}
