package romeplugin.messaging;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import romeplugin.MessageConstants;

import java.sql.SQLException;
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
            int messageCount;
            try {
                messageCount = notifications.messageCount(player.getUniqueId());
            } catch (SQLException e) {
                e.printStackTrace();
                player.sendMessage(MessageConstants.UWU_DATABASE_ERROR);
                return true;
            }
            var msg = notifications.getFirst(player.getUniqueId());
            player.sendMessage(msg + "\n<Message " + 1 + " / " + messageCount + ">");
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
            var messageCount = notifications.messageCount(player.getUniqueId());
            var n = Integer.parseInt(args[0]);
            if (n > messageCount) {
                player.sendMessage(MessageConstants.NOTIFICATION_INDEX_OUT_OF_BOUNDS);
                return true;
            }
            var msg = notifications.getIndex(player.getUniqueId(), n - 1);
            if (msg == null) {
                // this should never happen, but just in case :)
                player.sendMessage(MessageConstants.UWU_DATABASE_ERROR);
                return true;
            }
            player.sendMessage(msg + "\n<Message " + n + " / " + messageCount + ">");
        } catch (NumberFormatException e) {
            player.sendMessage("not a number..!");
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(MessageConstants.UWU_DATABASE_ERROR);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        // TODO: implement this
        return Collections.emptyList();
    }
}
