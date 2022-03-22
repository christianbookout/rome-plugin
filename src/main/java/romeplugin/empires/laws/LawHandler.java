package romeplugin.empires.laws;

import java.sql.SQLException;

import romeplugin.database.SQLConn;
import romeplugin.empires.EmpireHandler;
import romeplugin.empires.EmpireHandler.Empire;

public class LawHandler {
    public static final int MAX_DESCRIPTION_LENGTH = 100;
    private final EmpireHandler empireHandler;
    public LawHandler(EmpireHandler empireHandler) {
        this.empireHandler = empireHandler;
        try (var conn = SQLConn.getConnection()) {
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS empireLaws (" + //TODO make motions?
                    "number INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                    "empireName VARCHAR(50) NOT NULL," + // empire the law belongs to 
                    "description VARCHAR(" + MAX_DESCRIPTION_LENGTH + ") NOT NULL);").execute();
            // proposed laws
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS empireBills (" +
                    "name VARCHAR(50) NOT NULL PRIMARY KEY," +
                    "empireName VARCHAR(50) NOT NULL," +
                    "description VARCHAR(" + MAX_DESCRIPTION_LENGTH + ") NOT NULL," +
                    "votes INT NOT NULL DEFAULT 0);").execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * get a law by its name 
     * 
     * @param lawName
     * @return
     */
    public Law getLaw(String lawName) {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT * FROM empireLaws WHERE name = ?;");
            stmt.setString(1, lawName);
            var results = stmt.executeQuery();
            if (results.next()) {
                var empire = empireHandler.getEmpire(results.getString("empireName"));
                return new Law(results.getString("name"), results.getString("description"), empire);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a law
     * 
     * @param empireName
     * @param lawName
     * @param description
     */
    public void addLaw(String lawName, String empireName, String description) {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("INSERT INTO empireLaws VALUES (?, ?, ?);");
            stmt.setString(1, lawName);
            stmt.setString(2, empireName);
            stmt.setString(3, description);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove a law
     * 
     * @param lawName
     */
    public void removeLaw(String lawName) {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("DELETE * FROM empireLaws WHERE name = ?;");
            stmt.setString(1, lawName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if a law exists
     * 
     * @param lawName
     * @return true or false if the law exists or doesn't
     */
    public boolean lawExists(String lawName) {
    }

    class Law {
        private final String name, description;
        private final Empire empire;
        public Law(String name, String description, Empire empire) {
            this.name = name;
            this.description = description;
            this.empire = empire;
        }
        public String getName() {
            return this.name;
        }
        public String getDescription() {
            return this.description;
        }
        public Empire getEmpire() {
            return this.empire;
        }
    }
}
