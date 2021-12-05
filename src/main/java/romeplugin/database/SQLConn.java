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
            var res = read("SELECT * FROM players WHERE uuid = " + who.toString() + ";");
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
}