package romeplugin.database;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import romeplugin.title.Title;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class SQLConn {
    private static DataSource source;

    public static void setSource(DataSource src) {
        source = src;
    }

    public static Connection getConnection() throws SQLException {
        return source.getConnection();
    }

    public static Title getTitle(UUID who) {
        try (var conn = getConnection()) {
            var stmt = conn.prepareStatement("SELECT * FROM titles WHERE uuid = ?;");
            stmt.setString(1, who.toString());
            var res = stmt.executeQuery();
            if (!res.next()) {
                return null;
            }
            var titleName = res.getString("title");
            if (titleName == null) {
                return null;
            }
            return Title.getTitle(titleName);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Title getTitle(Player p) {
        try (var conn = getConnection()) {
            var stmt = conn.prepareStatement("SELECT title FROM titles WHERE uuid = ?;");
            stmt.setString(1, p.getUniqueId().toString());
            var res = stmt.executeQuery();
            if (!res.next()) {
                return null;
            }
            var titleName = res.getString("title");
            if (titleName == null) {
                return null;
            }
            return Title.getTitle(titleName);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ClaimEntry getClaim(int x, int y) {
        return getClaimRect(x, y, x, y);
    }

    public static ClaimEntry getClaim(Location loc) {
        return getClaim(loc.getBlockX(), loc.getBlockZ());
    }

    /**
     * @param uuid user to search for
     * @return the amount of extra blocks the user can claim (granted by whatever)
     */
    public static int getClaimAmount(UUID uuid) {
        try (var conn = getConnection()){
            var stmt = conn.prepareStatement("SELECT * FROM extraClaimBlocks WHERE uuid = ?");
            stmt.setString(1, uuid.toString());
            var results = stmt.executeQuery();
            if (results.next()) {
                return results.getInt("claimBlocks");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //TODO implement this :)
    /*public static void changeClaimAmount(UUID uuid) {
        try {
            var stmt = getConnection().prepareStatement("");
        } catch (SQLException e) {}
    }*/

    public static ClaimEntry getClaimRect(int x0, int y0, int x1, int y1) {
        ResultSet res = null;
        try (var conn = getConnection();
             var stmt = conn.prepareStatement("SELECT * FROM cityClaims WHERE x0 <= ? AND x1 >= ? AND y0 >= ? AND y1 <= ?;")) {
            try {
                stmt.setInt(1, x1);
                stmt.setInt(2, x0);
                stmt.setInt(3, y1);
                stmt.setInt(4, y0);
                res = stmt.executeQuery();
                if (!res.next()) {
                    return null;
                }
                return new ClaimEntry(
                        res.getInt("x0"),
                        res.getInt("y0"),
                        res.getInt("x1"),
                        res.getInt("y1"),
                        UUID.fromString(res.getString("owner_uuid")));
            } finally {
                if (res != null) {
                    res.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // does not verify the new claim doesn't overlap an existing claim!
    public static boolean addClaim(int x0, int y0, int x1, int y1, UUID uniqueId) {
        try (Connection conn = getConnection()) {
            var stmt = conn.prepareStatement("INSERT INTO cityClaims VALUES (?, ?, ?, ?, ?);");
            stmt.setInt(1, x0);
            stmt.setInt(2, y0);
            stmt.setInt(3, x1);
            stmt.setInt(4, y1);
            stmt.setString(5, uniqueId.toString());
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean removeClaim(ClaimEntry claim) {
        try (var conn = getConnection()) {
            var stmt = conn.prepareStatement("DELETE FROM cityClaims WHERE x0 = ? AND y0 = ? AND x1 = ? AND y1 = ? AND owner_uuid = ?;");
            stmt.setInt(1, claim.x0);
            stmt.setInt(2, claim.y0);
            stmt.setInt(3, claim.x1);
            stmt.setInt(4, claim.y1);
            stmt.setString(5, claim.owner.toString());
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getUsername(UUID uuid) {
        try (var conn = getConnection()) {
            var stmt = conn.prepareStatement("SELECT username FROM usernames WHERE uuid = ?;");
            stmt.setString(1, uuid.toString());
            var res = stmt.executeQuery();
            if (!res.next()) {
                return null;
            }
            return res.getString("username");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setUsername(UUID uuid, String name) {
        try (var conn = getConnection()) {
            var stmt = conn.prepareStatement("REPLACE INTO usernames VALUES (?, ?);");
            stmt.setString(1, uuid.toString());
            stmt.setString(2, name);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static UUID getUUIDFromUsername(String target) {
        try (var conn = getConnection()) {
            var stmt = conn.prepareStatement("SELECT uuid FROM usernames WHERE username = ?;");
            stmt.setString(1, target);
            var res = stmt.executeQuery();
            if (!res.next()) {
                return null;
            }
            return UUID.fromString(res.getString("uuid"));
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean updateClaimOwner(ClaimEntry entry, UUID newOwner) {
        try (var conn = getConnection()) {
            var stmt = conn.prepareStatement("UPDATE cityClaims SET owner_uuid = ?" +
                    "WHERE x0 = ? AND y0 = ? AND x1 = ? AND y1 = ?;");
            stmt.setString(1, newOwner.toString());
            stmt.setInt(2, entry.x0);
            stmt.setInt(3, entry.y0);
            stmt.setInt(4, entry.x1);
            stmt.setInt(5, entry.y1);
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean shareClaim(ClaimEntry entry, UUID toAdd) {
        try (var conn = getConnection()) {
            var exists = conn.prepareStatement("SELECT * FROM strawberry WHERE x0 = " + entry.x0 + " AND y0 = " + entry.y0 + " AND x1 = " + entry.x1 + " AND y1 = " + entry.y1 + " AND added_player_uuid = '" + toAdd.toString() + "';").executeQuery().next();
            if (exists) return false;
            var stmt = conn.prepareStatement("REPLACE INTO strawberry VALUES (?, ?, ?, ?, ?);");
            stmt.setInt(1, entry.x0);
            stmt.setInt(2, entry.y0);
            stmt.setInt(3, entry.x1);
            stmt.setInt(4, entry.y1);
            stmt.setString(5, toAdd.toString());
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean unshareClaim(ClaimEntry entry, UUID toRemove) {
        try (var conn = getConnection()) {
            var stmt = conn.prepareStatement("DELETE FROM strawberry WHERE x0 = ? AND y0 = ? AND x1 = ? AND y1 = ? AND added_player_uuid = ?;");
            stmt.setInt(1, entry.x0);
            stmt.setInt(2, entry.y0);
            stmt.setInt(3, entry.x1);
            stmt.setInt(4, entry.y1);
            stmt.setString(5, toRemove.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean claimShared(ClaimEntry entry, UUID sharedWith) {
        try (var conn = getConnection()) {
            var stmt = conn.prepareStatement("SELECT * FROM strawberry " +
                    "WHERE x0 = ? AND y0 = ? AND x1 = ? AND y1 = ? AND added_player_uuid = ?;");
            stmt.setInt(1, entry.x0);
            stmt.setInt(2, entry.y0);
            stmt.setInt(3, entry.x1);
            stmt.setInt(4, entry.y1);
            stmt.setString(5, sharedWith.toString());
            var result = stmt.executeQuery();
            return result.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getTotalClaimedBlocks(UUID who) {
        try (var conn = getConnection()) {
            var stmt = conn.prepareStatement("SELECT SUM((x1 - x0 + 1) * (y0 - y1 + 1)) FROM cityClaims WHERE owner_uuid = ?;");
            stmt.setString(1, who.toString());
            var res = stmt.executeQuery();
            if (!res.next()) {
                return 0;
            }
            return res.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return 9999;
        }
    }

    public static boolean isBuilder(UUID who) throws SQLException {
        try (var conn = getConnection()) {
            var stmt = conn.prepareStatement("SELECT * FROM builders WHERE uuid = ?;");
            stmt.setString(1, who.toString());
            var res = stmt.executeQuery();
            return res.next();
        }
    }

    public static void setBuilder(UUID who) throws SQLException {
        try (var conn = getConnection()) {
            var stmt = conn.prepareStatement("INSERT INTO builders VALUES (?);");
            stmt.setString(1, who.toString());
            stmt.execute();
        }
    }

    public static boolean removeBuilder(UUID who) throws SQLException {
        try (var conn = getConnection()) {
            var stmt = conn.prepareStatement("DELETE FROM builders WHERE uuid = ?;");
            stmt.setString(1, who.toString());
            return stmt.executeUpdate() > 0;
        }
    }

    public static List<String> getBuilders() {
        try (var conn = getConnection()) {
            var stmt = conn.prepareStatement("SELECT username FROM usernames WHERE uuid IN (SELECT uuid FROM builders);");
            var res = stmt.executeQuery();
            var names = new ArrayList<String>();
            while (res.next()) {
                names.add(res.getString("username"));
            }
            return names;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<UUID> getUUIDsWithTitle(Title title) {
        try (var conn = getConnection()) {
            var stmt = conn.prepareStatement("SELECT uuid FROM titles WHERE title = ?;");
            stmt.setString(1, title.toString());
            var res = stmt.executeQuery();
            var names = new ArrayList<UUID>();
            while (res.next()) {
                names.add(UUID.fromString(res.getString("uuid")));
            }
            return names;
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}