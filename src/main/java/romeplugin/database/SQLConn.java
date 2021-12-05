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
            if (res == null) {
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
        try {
            var stmt = getConnection().prepareStatement("SELECT * FROM cityClaims WHERE x0 <= ? AND x1 >= ? AND y0 >= ? AND y1 <= ?;");
            stmt.setInt(1, x);
            stmt.setInt(2, x);
            stmt.setInt(3, y);
            stmt.setInt(4, y);
            var res = stmt.executeQuery();
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
}