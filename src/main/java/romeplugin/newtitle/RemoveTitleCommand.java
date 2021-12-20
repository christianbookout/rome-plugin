package romeplugin.newtitle;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import romeplugin.RomePlugin;
import romeplugin.database.SQLConn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class RemoveTitleCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] params) {
        if (params.length < 1) {
            sender.sendMessage(ChatColor.RED + "sina wile e jan!");
            return false;
        }
        Player target = sender.getServer().getPlayer(params[0]);
        UUID uuid;
        if (target == null) {
            uuid = SQLConn.getUUIDFromUsername(params[0]);
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "ni jan li lon ala!");
                return false;
            }
        } else {
            uuid = target.getUniqueId();
        }
        try (Connection conn = SQLConn.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(
                    "DELETE FROM players WHERE uuid = ?;");
            statement.setString(1, uuid.toString());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "Eskewel li pali ike!");
            return false;
        }
        if (target != null) {
            RomePlugin.onlinePlayerTitles.remove(target);
        }
        sender.sendMessage("weka e nimi wawa lon jan " + params[0] + ".");
        return true;
    }
}
