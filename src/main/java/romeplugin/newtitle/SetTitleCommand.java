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
    private final PermissionsHandler perms;

    public SetTitleCommand(PermissionsHandler perms) {
        this.perms = perms;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] params) {
        if (params.length < 2) {
            return false;
        }
        Title title;
        try {
            title = Title.getTitle(params[1]);
        } catch (IllegalArgumentException e) {
            commandSender.sendMessage("invalid title :)");
            return false;
        }
        Player target = commandSender.getServer().getPlayer(params[0]);
        if (target == null) {
            return false;
        }

        if (!setTitle(target, title)) {
            return false;
        }

        var oldTitle = RomePlugin.onlinePlayerTitles.put(target, title);
        perms.updateTitle(target, title, oldTitle);

        commandSender.sendMessage(target.getDisplayName() + "'s title is now " + title.toString());
        return true;
    }

    /**
     * set title of player
     *
     * @param who   target player
     * @param title target title
     * @return true on success, false on failure
     */
    public static boolean setTitle(Player who, Title title) {
        try (Connection conn = SQLConn.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(
                    "REPLACE INTO players (uuid, title) values (?, ?);");
            statement.setString(1, who.getUniqueId().toString());
            statement.setString(2, title.toString());
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
