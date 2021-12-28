package romeplugin.newtitle;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import romeplugin.RomePlugin;
import romeplugin.database.SQLConn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class TitleHandler {
    private final PermissionsHandler perms;

    public TitleHandler(Plugin plugin) {
        perms = new PermissionsHandler(plugin);
    }

    public void playerJoin(Player player) {
        var title = SQLConn.getTitle(player.getUniqueId());
        perms.playerJoin(player, title);
        if (title == null) {
            return;
        }
        RomePlugin.onlinePlayerTitles.put(player, title.t);
    }

    public void playerQuit(Player player) {
        perms.playerQuit(player);
        RomePlugin.onlinePlayerTitles.remove(player);
    }

    public boolean setTitle(Player player, Title title) {
        if (!setTitleOffline(player.getUniqueId(), title)) {
            return false;
        }

        var oldTitle = RomePlugin.onlinePlayerTitles.put(player, title);
        perms.updateTitle(player, title, oldTitle);
        return true;
    }

    boolean setTitleOffline(UUID uuid, Title title) {
        try (Connection conn = SQLConn.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(
                    "REPLACE INTO players (uuid, title) values (?, ?);");
            statement.setString(1, uuid.toString());
            statement.setString(2, title.toString());
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeTitle(Player player) {
        if (!removeTitleOffline(player.getUniqueId())) {
            return false;
        }

        var oldTitle = RomePlugin.onlinePlayerTitles.remove(player);
        if (oldTitle != null) {
            perms.deleteTitle(player.getUniqueId(), oldTitle);
        }
        return true;
    }

    public boolean removeTitleOffline(UUID uuid) {
        try (Connection conn = SQLConn.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(
                    "DELETE FROM players WHERE uuid = ?;");
            statement.setString(1, uuid.toString());
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
