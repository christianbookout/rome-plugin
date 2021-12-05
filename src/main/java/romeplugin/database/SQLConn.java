package romeplugin.database;

import romeplugin.newtitle.Title;

import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;


public class SQLConn {
    private static DataSource source;

    public static void setSource(DataSource src) {
        source = src;
    }

    public static Connection getConnection() throws SQLException {
        return source.getConnection();
    }

    public static ResultSet read(String SQLCode) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement(SQLCode);
            return statement.executeQuery();
        } catch (SQLTimeoutException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static TitleEntry getTitle(UUID who) {
        try {
            var stmt = getConnection().prepareStatement("SELECT * FROM players WHERE uuid = ?;");
            stmt.setString(1, who.toString());
            var res = stmt.executeQuery();
            if (!res.isBeforeFirst()) {
                return null;
            }
            var titleName = res.getString("title");
            if (titleName == null) {
                return null;
            }
            return new TitleEntry(Title.getTitle(titleName), who);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ClaimEntry getClaim(int x, int y) {
        return getClaimRect(x, y, x, y);
    }

    public static ClaimEntry getClaimRect(int x0, int y0, int x1, int y1) {
        try {
            var stmt = getConnection().prepareStatement("SELECT * FROM cityClaims WHERE " +
                    "x0 <= ? AND x1 >= ? AND y0 >= ? AND y1 <= ?;");
            stmt.setInt(1, x1);
            stmt.setInt(2, x0);
            stmt.setInt(3, y1);
            stmt.setInt(4, y0);
            var res = stmt.executeQuery();
            if (!res.isBeforeFirst()) {
                return null;
            }
            return new ClaimEntry(
                    res.getInt("x0"),
                    res.getInt("y0"),
                    res.getInt("x1"),
                    res.getInt("y1"),
                    UUID.fromString(res.getString("owner_uuid")));
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // does not verify the new claim doesn't overlap an existing claim!
    public static void addClaim(int x0, int y0, int x1, int y1, UUID uniqueId) {
        try {
            var stmt = getConnection().prepareStatement("INSERT INTO cityClaims VALUES (?, ?, ?, ?, ?);");
            stmt.setInt(1, x0);
            stmt.setInt(2, y0);
            stmt.setInt(3, x1);
            stmt.setInt(4, y1);
            stmt.setString(5, uniqueId.toString());
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}