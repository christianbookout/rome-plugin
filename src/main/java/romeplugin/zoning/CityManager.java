package romeplugin.zoning;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;
import romeplugin.empires.role.RoleHandler;
import romeplugin.zoning.claims.City;
import romeplugin.zoning.claims.CityChunks;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class CityManager {
    private final ArrayList<City> cities = new ArrayList<>();
    private final CityChunks chunks;
    private final int initialGovernmentSize;
    private final int cityMult;
    private final int suburbsMult;
    private final int minBlockLimit;
    private final RoleHandler roleHandler;
    private final static int INITIAL_CITY_CHUNK_RADIUS = 5;

    public CityManager(int initialGovernmentSize, int cityMult, int suburbsMult, int minBlockLimit, RoleHandler roleHandler) {
        this.initialGovernmentSize = initialGovernmentSize;
        this.cityMult = cityMult;
        this.suburbsMult = suburbsMult;
        this.minBlockLimit = minBlockLimit;
        this.roleHandler = roleHandler;
        makeTables();
        getDatabaseCities();
        chunks = new CityChunks();
    }

    private void makeTables() {
        try (var conn = SQLConn.getConnection()) {
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS cityInfo (" +
                    "id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "size INT NOT NULL," +
                    "x INT NOT NULL," +
                    "y INT NOT NULL," +
                    "name VARCHAR(20) NOT NULL," +
                    "founder_uuid CHAR(36) NOT NULL," +
                    "found_date DATE NOT NULL);").execute();
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS cityMembers (" +
                    "uuid CHAR(36) NOT NULL PRIMARY KEY," +
                    "cityId INT UNSIGNED NOT NULL);").execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public City getCity(Location loc) {
        for (var city : cities) {
            if (city.inCity(loc)) {
                return city;
            }
        }
        return null;
    }

    public boolean inCity(Location location) {
        return getCity(location) != null;
    }

    public CityArea getArea(Location location) {
        var city = getCity(location);
        if (city != null) {
            return city.getArea(location);
        }
        return null;
    }

    private City intersectingCity(int x0, int y0, int x1, int y1) {
        for (var city : cities) {
            if (city.cityIntersects(x0, y0, x1, y1)) {
                return city;
            }
        }
        return null;
    }

    public boolean tryClaimLand(Player player, int xa, int ya, int xb, int yb) {
        // ensure x0, y0 is the top left point and x1, y1 is the bottom right point
        var x0 = Math.min(xa, xb);
        var y0 = Math.max(ya, yb);
        var x1 = Math.max(xa, xb);
        var y1 = Math.min(ya, yb);
        var city = intersectingCity(x0, y0, x1, y1);
        if (city == null) {
            // no city!
            player.sendMessage(ChatColor.RED + "NO CLAIM IN WILDERNESS");
            return false;
        }
        return city.tryClaimLand(player, x0, y0, x1, y1);
    }

    public boolean isOutsideSuburbs(Location location) {
        var city = getCity(location);
        if (city == null)
            return true;
        return city.isOutsideSuburbs(location);
    }

    public boolean canBreak(Player player, Location location) {
        var city = getCity(location);
        if (city == null)
            return true;
        return city.canBreak(player, location);
    }

    public boolean inWilderness(Location location) {
        var city = getCity(location);
        if (city == null)
            return true;
        return city.inWilderness(location);
    }

    public City getCityByName(String name) {
        for (var city : cities) {
            if (city.getName().equals(name)) {
                return city;
            }
        }
        return null;
    }

    /**
     * synchronizes the local copy of the cities list with the one on the database
     */
    private void getDatabaseCities() {
        cities.clear();
        try (var conn = SQLConn.getConnection()) {
            var res = conn.prepareStatement("SELECT * FROM cityInfo;").executeQuery();
            while (res.next()) {
                cities.add(new City(
                        res.getInt("x"),
                        res.getInt("y"),
                        res.getInt("size"),
                        res.getInt("id"),
                        res.getString("name"),
                        cityMult,
                        suburbsMult,
                        minBlockLimit,
                        roleHandler));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Optional<City> getPlayerCity(UUID uuid) {
        try (var conn = SQLConn.getConnection()) {
            // TODO: we should really be able to get a city by its id
            var stmt = conn.prepareStatement("SELECT name FROM cityInfo WHERE id = (SELECT cityId FROM cityMembers WHERE uuid = ?);");
            stmt.setString(1, uuid.toString());
            var res = stmt.executeQuery();
            if (!res.next()) {
                return Optional.empty();
            }
            return Optional.of(getCityByName(res.getString(1)));
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public void foundCity(Player player, String name) {
        var loc = player.getLocation();
        var extents = initialGovernmentSize * cityMult;
        int cityX = loc.getBlockX();
        int cityZ = loc.getBlockZ();
        if (intersectingCity(cityX - extents, cityZ + extents, cityX + extents, cityZ - extents) != null) {
            player.sendMessage("city intersection error");
            return;
        }
        int chunkX = loc.getChunk().getX();
        int chunkZ = loc.getChunk().getZ();
        if (chunks.rectIntersectsCity(
                chunkX - INITIAL_CITY_CHUNK_RADIUS,
                chunkZ + INITIAL_CITY_CHUNK_RADIUS,
                chunkX + INITIAL_CITY_CHUNK_RADIUS,
                chunkZ - INITIAL_CITY_CHUNK_RADIUS
        )) {
            return;
        }
        int cityId;
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("INSERT INTO cityInfo (size, x, y, name, founder_uuid, found_date) VALUES (?, ?, ?, ?, ?, CURDATE());");
            stmt.setInt(1, initialGovernmentSize);
            stmt.setInt(2, cityX);
            stmt.setInt(3, cityZ);
            stmt.setString(4, name);
            stmt.setString(5, player.getUniqueId().toString());
            if (stmt.executeUpdate() < 1) {
                throw new SQLException("failed to insert new city?!");
            }
            var last_insert_stmt = conn.prepareStatement("SELECT LAST_INSERT_ID();");
            var res = last_insert_stmt.executeQuery();
            res.next();
            cityId = res.getInt(1);
        } catch (SQLException e) {
            player.sendMessage(MessageConstants.UWU_DATABASE_ERROR);
            e.printStackTrace();
            return;
        }
        if (!chunks.claimChunkRect(cityId,
                chunkX - INITIAL_CITY_CHUNK_RADIUS,
                chunkZ + INITIAL_CITY_CHUNK_RADIUS,
                chunkX + INITIAL_CITY_CHUNK_RADIUS,
                chunkZ - INITIAL_CITY_CHUNK_RADIUS)) {
            player.sendMessage(MessageConstants.UWU_DATABASE_ERROR);
            return;
        }
        player.sendMessage("made city");
        cities.add(new City(cityX, cityZ, initialGovernmentSize, cityId, name, cityMult, suburbsMult, minBlockLimit, roleHandler));
    }

    public void expandGovernment(String name, int amount) {
        var city = getCityByName(name);
        if (city == null) {
            return;
        }
        if (!city.expandGovernment(amount)) {
            return;
        }
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("UPDATE cityInfo SET size = size + ? WHERE name = ?;");
            stmt.setInt(1, amount);
            stmt.setString(2, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getClaimedBlocksInSuburbs(UUID uuid) {
        return getPlayerCity(uuid).map(city -> city.getClaimedBlocksInSuburbs(uuid)).orElse(0);
    }
}
