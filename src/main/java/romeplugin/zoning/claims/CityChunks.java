package romeplugin.zoning.claims;

import romeplugin.database.SQLConn;

import java.sql.SQLException;
import java.util.OptionalInt;

public class CityChunks {
    CityChunks() {
        makeTables();
    }

    private void makeTables() {
        try (var conn = SQLConn.getConnection()) {
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS cityChunks(" +
                    "cityId INT UNSIGNED NOT NULL," +
                    "x INT NOT NULL," +
                    "z INT NOT NULL," +
                    "PRIMARY KEY (x, z)," +
                    "FOREIGN KEY (cityId) REFERENCES cityInfo(id));");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public OptionalInt getChunkOwner(int chunkX, int chunkZ) throws SQLException {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT cityId FROM cityChunks WHERE x=? AND z=?;");
            stmt.setInt(1, chunkX);
            stmt.setInt(2, chunkZ);
            var res = stmt.executeQuery();
            if (res.next()) {
                return OptionalInt.of(res.getInt("cityId"));
            }
            return OptionalInt.empty();
        }
    }

    public boolean rectIntersectsCity(int left, int top, int right, int bottom) {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT EXISTS(SELECT * FROM cityChunks WHERE" +
                    "x >= ? AND" +
                    "x <= ? AND" +
                    "z >= ? AND" +
                    "z <= ?);");
            stmt.setInt(1, left);
            stmt.setInt(2, right);
            stmt.setInt(3, bottom);
            stmt.setInt(4, top);
            var res = stmt.executeQuery();
            var hasNext = res.next();
            assert hasNext;
            return res.getBoolean(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }
}
