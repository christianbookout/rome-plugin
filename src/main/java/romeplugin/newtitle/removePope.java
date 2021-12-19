package romeplugin.newtitle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import romeplugin.RomePlugin;
import romeplugin.database.SQLConn;
import romeplugin.database.TitleEntry;

public class removePope implements Listener {
    
    @EventHandler
    public boolean onDeathEvent(PlayerDeathEvent e) {
        TitleEntry titleEntry = SQLConn.getTitle(e.getEntity().getUniqueId());
        if (titleEntry != null && titleEntry.t == Title.POPE) {
            try (Connection conn = SQLConn.getConnection()) {
                PreparedStatement statement = conn.prepareStatement(
                        "DELETE FROM players WHERE uuid = ?;");
                statement.setString(1, e.getEntity().getUniqueId().toString());
                statement.execute();
            } catch (SQLException exc) {
                exc.printStackTrace();
                return false;
            }
            RomePlugin.onlinePlayerTitles.remove(e.getEntity());
        }
        return true;
    }
}
