package romeplugin.empires.laws;

import romeplugin.database.SQLConn;
import romeplugin.empires.EmpireHandler;
import romeplugin.empires.EmpireHandler.Empire;

import java.sql.SQLException;
import java.util.ArrayList;

// motions start with M and laws start with C 
public class LawHandler {
    public enum LawType {
        BILL("empireLaws", 'M'), MOTION("empireMotions", 'C');

        private final String tableName;
        public final char typePrefix;

        LawType(final String tableName, char typePrefix) {
            this.tableName = tableName;
            this.typePrefix = typePrefix;
        }

        public String getTable() {
            return this.tableName;
        }

        public static LawType fromPrefix(char prefix) {
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
                    "number INT NOT NULL AUTO_INCREMENT," +
                    "empireId INT UNSIGNED NOT NULL," + // empire the law belongs to
                    "description VARCHAR(" + MAX_DESCRIPTION_LENGTH + ") NOT NULL," +
                    "PRIMARY KEY (number, empireId));").execute();

            conn.prepareStatement("CREATE TABLE IF NOT EXISTS empireMotions (" +
                    "number INT NOT NULL AUTO_INCREMENT," +
                    "empireId INT UNSIGNED NOT NULL," +
                    "description VARCHAR(" + MAX_DESCRIPTION_LENGTH + ") NOT NULL," +
                    "PRIMARY KEY (number, empireId));").execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * parse an int out of a law name.
     * law names are formatted M-104 for motions and C-31 for bills
     *
     * @param lawName full name of law
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
     * @return the law type
     */
    private static LawType parseLawType(String lawName) {
        if (lawName.isEmpty()) return null;
        var firstChar = lawName.toCharArray()[0];
        return LawType.fromPrefix(firstChar);
    }

    private static LawType parseLawTypeStrict(String lawName) {
        var lawType = parseLawType(lawName);
        if (lawType == null) {
            throw new IllegalArgumentException("invalid law name");
        }
        return lawType;
    }

    /**
     * get a law by its name
     *
     * @param lawName full name of law
     * @return the law in the database, or null if there are none that match lawName
     */
    public Law getLaw(String lawName, int empireId) {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT * FROM ? WHERE name = ? AND empireId = ?;");
            stmt.setString(1, LawHandler.parseLawTypeStrict(lawName).getTable());
            stmt.setString(2, lawName);
            stmt.setInt(3, empireId);
            var results = stmt.executeQuery();
            if (results.next()) {
                var empire = empireHandler.getEmpireById(results.getInt("empireId"));
                return new Law(results.getInt("number"), results.getString("description"), empire, LawHandler.parseLawType(lawName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Law> getLawsList(int empireId, int maxLaws) {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT * FROM empireLaws WHERE empireId = ? LIMIT ?;");
            stmt.setInt(1, empireId);
            stmt.setInt(2, maxLaws);
            var results = stmt.executeQuery();
            var laws = new ArrayList<Law>();
            while (results.next()) {
                laws.add(new Law(results.getInt("number"), results.getString("description"), null, LawType.BILL));
            }
            return laws;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * create a law
     *
     * @param empireId    id of the target empire
     * @param lawName     full name of the law
     * @param description description of the law
     */
    public void addLaw(String lawName, int empireId, String description) {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("INSERT INTO empireLaws VALUES (?, ?, ?);");
            stmt.setString(1, lawName);
            stmt.setInt(2, empireId);
            stmt.setString(3, description);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove a law
     */
    public boolean removeLaw(String lawName, int empireId) {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("DELETE * FROM empireLaws WHERE name = ? AND empireId = ?;");
            stmt.setString(1, lawName);
            stmt.setInt(2, empireId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check if a law exists
     *
     * @param lawName  full name of law
     * @param empireId id of the empire the law belongs to
     * @return true or false if the law exists or doesn't
     */
    public boolean lawExists(String lawName, int empireId) {
        return this.getLaw(lawName, empireId) != null;
    }

    static class Law {
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
