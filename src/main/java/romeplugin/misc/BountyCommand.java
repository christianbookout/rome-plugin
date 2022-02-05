package romeplugin.misc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import romeplugin.database.SQLConn;

public class BountyCommand implements CommandExecutor, TabCompleter{
    public BountyCommand() {
        try (Connection conn = SQLConn.getConnection()){
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS bounties (" +
                                  "username CHAR(32) NOT NULL)," +
                                  "target_uuid CHAR(36) NOT NULL," +
                                  "item VARCHAR(100) NOT NULL," +
                                  "count INT NOT NULL," +
                                  ");").execute();
        } catch (SQLException e) {
            
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // TODO Auto-generated method stub
        return null;
    }
}
