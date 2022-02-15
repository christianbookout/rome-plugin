package romeplugin.zoning;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;
import romeplugin.zoning.claims.City;

import java.sql.SQLException;
import java.util.ArrayList;

public class CityManager {
    private final ArrayList<City> cities = new ArrayList<>();
    private final int initialGovernmentSize;
    private final int cityMult;
    private final int suburbsMult;
    private final int minBlockLimit;

    public CityManager(int initialGovernmentSize, int cityMult, int suburbsMult, int minBlockLimit) {
        this.initialGovernmentSize = initialGovernmentSize;
        this.cityMult = cityMult;
        this.suburbsMult = suburbsMult;
        this.minBlockLimit = minBlockLimit;
        getDatabaseCities();
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

    public boolean inSuburbs(Location location) {
        var city = getCity(location);
        if (city == null)
            return false;
        return city.inSuburbs(location);
    }

    public boolean canBreak(Player player, Location location) {
        var city = getCity(location);
        if (city == null)
            return false;
        return city.canBreak(player, location);
    }

    public boolean inWilderness(Location location) {
        var city = getCity(location);
        if (city == null)
            return true;
        return city.inWilderness(location);
    }

    private void getDatabaseCities() {
        try (var conn = SQLConn.getConnection()) {
            var res = conn.prepareStatement("SELECT * FROM cityInfo;").executeQuery();
            while (res.next()) {
                cities.add(new City(
                        res.getInt("x"),
                        res.getInt("y"),
                        res.getInt("size"),
                        cityMult,
                        suburbsMult,
                        minBlockLimit
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void foundCity(Player player, String name) {
        var loc = player.getLocation();
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("INSERT INTO cityInfo (size, x, y, name, founder_uuid, found_date) VALUES (?, ?, ?, ?, ?, CURDATE());");
            stmt.setInt(1, initialGovernmentSize);
            stmt.setInt(2, loc.getBlockX());
            stmt.setInt(3, loc.getBlockZ());
            stmt.setString(4, name);
            stmt.setString(5, player.getUniqueId().toString());
            if (stmt.executeUpdate() < 1) {
                throw new SQLException("failed to insert new city?!");
            }
        } catch (SQLException e) {
            player.sendMessage(MessageConstants.UWU_DATABASE_ERROR);
            e.printStackTrace();
            return;
        }
        cities.add(new City(loc.getBlockX(), loc.getBlockZ(), initialGovernmentSize, cityMult, suburbsMult, minBlockLimit));
    }
}
