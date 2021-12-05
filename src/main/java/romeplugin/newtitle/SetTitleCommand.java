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

public class SetTitleCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] params) {
        if (params.length < 2) {
            return false;
        }
        Title title;
        try {
            title = Title.getTitle(params[1]);
        } catch (IllegalArgumentException e) {
            return false;
        }
        Player target = commandSender.getServer().getPlayer(params[0]);
        if (target == null) {
            return false;
        }

        try (Connection conn = SQLConn.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(
                    "REPLACE INTO players (uuid, title) values (?, ?);");
            statement.setString(1, target.getUniqueId().toString());
            statement.setString(2, params[1]);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        RomePlugin.onlinePlayerTitles.replace(target, title);
        return true;
    }
}
