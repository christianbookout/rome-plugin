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
 



}