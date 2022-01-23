package romeplugin.title;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import romeplugin.RomePlugin;
import romeplugin.database.SQLConn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RemovePopeListener implements Listener {

    @EventHandler
    public void onDeathEvent(PlayerDeathEvent e) {
        Title titleEntry = SQLConn.getTitle(e.getEntity().getUniqueId());
        if (titleEntry == Title.POPE) {
            try (Connection conn = SQLConn.getConnection()) {
                PreparedStatement statement = conn.prepareStatement(
                        "DELETE FROM titles WHERE uuid = ?;");
                statement.setString(1, e.getEntity().getUniqueId().toString());
                statement.execute();
            } catch (SQLException exc) {
                exc.printStackTrace();
                return;
            }
            RomePlugin.onlinePlayerTitles.remove(e.getEntity());
        }
    }
}
