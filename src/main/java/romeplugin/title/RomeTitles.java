package romeplugin.title;

import org.bukkit.ChatColor;

import romeplugin.database.SQLConn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map.Entry;

public class RomeTitles {
    private final HashMap<UUID, Title> playerTitles;
    private final HashMap<String, Title> knownTitles;

    public RomeTitles() {
        playerTitles = new HashMap<>();
        knownTitles = new HashMap<>();
    }

    public boolean isTitle(String title) {
        return knownTitles.containsKey(title);
    }

    public void addTitle(String title) {
        addTitle(title, ChatColor.WHITE);
    }

    public void addTitle(String title, ChatColor color) {
        knownTitles.put(title, new Title(title, color));
    }

    public Title getKnownTitle(String title) {
        return knownTitles.get(title);
    }
    public Collection<Title> getAllKnownTitles() { return knownTitles.values(); }

    public Title getPlayerTitle(UUID uuid) {
        return playerTitles.get(uuid);
    }

    public void setPlayerTitle(UUID uuid, String title) {
        playerTitles.put(uuid, getKnownTitle(title));
    }

    public void saveData(SQLConn conn) throws SQLException {
        for (Entry<UUID, Title> entry : playerTitles.entrySet()) {
            try (Connection connection = conn.getConnection()) {
                String SQL = "INSERT INTO playertitles (title, mostSig, leastSig) values (? ? ?)";
                PreparedStatement statement = connection.prepareStatement(SQL);
                statement.setString(1, entry.getValue().name);
                statement.setLong(2, entry.getKey().getMostSignificantBits());
                statement.setLong(3, entry.getKey().getLeastSignificantBits());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadData(SQLConn conn) throws SQLException {
        String SQL = "SELECT * FROM playertitles";
        ResultSet results = conn.read(SQL);
        while (results.next()) {
            UUID id = new UUID(results.getLong("mostSig"), results.getLong("leastSig"));
            String titleName = results.getString("title");
            playerTitles.put(id, getKnownTitle(titleName));
        }
    }
}
