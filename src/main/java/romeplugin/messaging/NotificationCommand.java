package romeplugin.messaging;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import romeplugin.MessageConstants;

import java.util.Collections;
import java.util.List;

public class NotificationCommand implements CommandExecutor, TabCompleter {
    private final NotificationQueue notifications;

    public NotificationCommand(NotificationQueue notifications) {
        this.notifications = notifications;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        var player = (Player) sender;
        if (args.length == 0) {
            var msg = notifications.getFirst(player.getUniqueId());
            player.sendMessage(msg);
            return true;
        }
        if ("clear".equals(args[0])) {
            MessageConstants.sendOnSuccess(
                    notifications.clear(player.getUniqueId()),
                    player,
                    MessageConstants.SUCCESSFUL_NOTIFICATION_CLEAR
            );
            return true;
        }
        try {
            var n = Integer.parseInt(args[0]);
            // TODO: implement getting nth notification
        } catch (NumberFormatException e) {
            player.sendMessage("not a number..!");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        // TODO: implement this
        return Collections.emptyList();
    }
}
