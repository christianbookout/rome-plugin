package romeplugin.messaging;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;

import java.sql.SQLException;
import java.util.UUID;

public class NotificationQueue {
    public NotificationQueue() {
        try (var conn = SQLConn.getConnection()) {
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS notificationQueue (" +
                    "uuid CHAR(36) NOT NULL," +
                    "message VARCHAR(500) NOT NULL," +
                    "priority SMALLINT UNSIGNED NOT NULL);");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void sendNotification(Player target, String message) {
        insertLast(target.getUniqueId(), message);
        target.sendMessage(MessageConstants.NOTIFICATION_RECEIVED);
    }

    public void sendNotification(UUID target, String message) {
        insertLast(target, message);
        var player = Bukkit.getPlayer(target);
        if (player != null) {
            player.sendMessage(MessageConstants.NOTIFICATION_RECEIVED);
        }
    }

    public void broadcastNotification(String message) {
        var uuids = SQLConn.getAllUUIDs();
        if (uuids == null) {
            return;
        }
        for (var uuid : uuids) {
            insertLast(uuid, message);
        }
    }

    public int messageCount(UUID target) throws SQLException {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT COUNT(*) FROM notificationQueue WHERE uuid=?;");
            stmt.setString(1, target.toString());
            var res = stmt.executeQuery();
            res.next();
            return res.getInt(1);
        }
    }

    public void insertLast(UUID target, String message) {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("INSERT INTO notificationQueue VALUE (?, ?, (SELECT COUNT(*) FROM notificationQueue WHERE uuid=?));");
            stmt.setString(1, target.toString());
            stmt.setString(2, message);
            stmt.setString(3, target.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getFirst(UUID target) {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT message FROM notificationQueue WHERE uuid=? ORDER BY priority ASC LIMIT 1;");
            stmt.setString(1, target.toString());
            var res = stmt.executeQuery();
            if (res.next()) {
                return res.getString("message");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void removeFirst(UUID target) {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("DELETE FROM notificationQueue WHERE uuid=? ORDER BY priority ASC LIMIT 1;");
            stmt.setString(1, target.toString());
            stmt.executeUpdate();
            stmt.close();
            stmt = conn.prepareStatement("UPDATE notificationQueue SET priority = priority - 1 WHERE uuid=?;");
            stmt.setString(1, target.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean clear(UUID target) {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("DELETE FROM notificationQueue WHERE uuid=?;");
            stmt.setString(1, target.toString());
            stmt.executeUpdate();
            stmt.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
