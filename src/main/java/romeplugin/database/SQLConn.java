package romeplugin.database;
import java.sql.*;


public class SQLConn { //FIXME: please god
    private static String username, password, url;

    public static void setStuff(String url, String username, String password) {
        SQLConn.username = username;
        SQLConn.password = password;
        SQLConn.url = url;
    }

    public static Connection getConnection() throws SQLException{
        if (username == null) return null;
        return DriverManager.getConnection(url, username, password);
    }

    public static ResultSet read(String SQLCode) throws SQLException {
        if (username == null) return null;
        try (Connection conn = getConnection()){
            PreparedStatement statement = conn.prepareStatement(SQLCode);
            return statement.executeQuery();
        } catch (SQLTimeoutException e) {
            e.printStackTrace();
            return null;
        }
    }
 



}