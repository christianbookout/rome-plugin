package romeplugin.empires.laws;

import java.sql.SQLException;

import romeplugin.database.SQLConn;
import romeplugin.empires.EmpireHandler;
import romeplugin.empires.EmpireHandler.Empire;

// motions start with M and laws start with C 
public class LawHandler {
    public enum LawType {
        BILL("empireLaws"), MOTION("empireMotions");

        private final String tableName;
        private LawType(final String tableName) {
            this.tableName = tableName; 
        }
        public String getTable() { return this.tableName; }
        public static LawType fromPrefix (char prefix) {
            switch (prefix) {
                case 'M':
                    return LawType.MOTION;
                case 'C':
                    return LawType.BILL;
                default: 
                    return null;
            }
        }
    }
    public static final int MAX_DESCRIPTION_LENGTH = 100;
    private final EmpireHandler empireHandler;
    public LawHandler(EmpireHandler empireHandler) {
        this.empireHandler = empireHandler;
        try (var conn = SQLConn.getConnection()) {
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS empireLaws (" + // TODO refactor to fit all empires uniquely (number)
                    "number INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                    "empireName VARCHAR(50) NOT NULL," + // empire the law belongs to 
                    "description VARCHAR(" + MAX_DESCRIPTION_LENGTH + ") NOT NULL);").execute();

            conn.prepareStatement("CREATE TABLE IF NOT EXISTS empireMotions (" + 
                    "number INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                    "empireName VARCHAR(50) NOT NULL," +
                    "description VARCHAR(" + MAX_DESCRIPTION_LENGTH + ") NOT NULL);").execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * parse an int out of a law name.
     * law names are formatted M-104 for motions and C-31 for bills
     * 
     * @param lawName
     * @return the parsed number
     */
    private static int parseInt(String lawName) {
        try {
            return Integer.parseInt(lawName.substring(2));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * parse a law type out of a law name
     * 
     * @param lawName
     * @return the law type
     */
    private static LawType parseLawType(String lawName) {
        if (lawName.isEmpty()) return null;
        var firstChar = lawName.toCharArray()[0];
        return LawType.fromPrefix(firstChar);
    }
    /**
     * get a law by its name 
     * 
     * @param lawName
     * @return the law in the database, or null if there are none that match lawName
     */
    public Law getLaw(String lawName, String empireName) {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT * FROM ? WHERE name = ? AND empireName = ?;");
            stmt.setString(1, LawHandler.parseLawType(lawName).getTable());
            stmt.setString(2, lawName);
            var results = stmt.executeQuery();
            if (results.next()) {
                var empire = empireHandler.getEmpire(results.getString("empireName"));
                return new Law(results.getInt("number"), results.getString("description"), empire, LawHandler.parseLawType(lawName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * create a law
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
    public boolean removeLaw(String lawName, String empireName) {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("DELETE * FROM empireLaws WHERE name = ? AND empireName = ?;");
            stmt.setString(1, lawName);
            stmt.setString(2, empireName);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check if a law exists
     * 
     * @param lawName
     * @return true or false if the law exists or doesn't
     */
    public boolean lawExists(String lawName) {
        return this.getLaw(lawName) != null;
    }

    class Law {
        private final String description;
        private final int number;
        private final LawType lawType;
        private final Empire empire;
        public Law(int number, String description, Empire empire, LawType lawType) {
            this.lawType = lawType;
            this.number = number;
            this.description = description;
            this.empire = empire;
        }
        public int getNumber() {
            return this.number;
        }
        public String getDescription() {
            return this.description;
        }
        public Empire getEmpire() {
            return this.empire;
        }
        public LawType getLawType() {
            return this.lawType;
        }
    }
}
