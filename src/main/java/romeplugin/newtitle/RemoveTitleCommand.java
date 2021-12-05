package romeplugin.newtitle;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import romeplugin.RomePlugin;
import romeplugin.database.SQLConn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RemoveTitleCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] params) {
        if (params.length < 2) {
            return false;
        }
        Player target = commandSender.getServer().getPlayer(params[0]);
        if (target == null) {
            return false;
        }
        try (Connection conn = SQLConn.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(
                    "DELETE FROM players WHERE uuid = ?;");
            statement.setString(1, target.getUniqueId().toString());
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        RomePlugin.onlinePlayerTitles.remove(target);
        return true;
    }
}
