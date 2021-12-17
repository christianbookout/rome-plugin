package romeplugin.zoning;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import romeplugin.RomePlugin;
import romeplugin.database.SQLConn;
import romeplugin.newtitle.Title;

import java.sql.SQLException;

import static romeplugin.zoning.ZoneType.*;

public class LandControl {

    private CityArea[] areas;
    private int cityX, cityY;
    private int governmentSize;
    private final int cityMult;
    private final int suburbsMult;

    public LandControl(int cityX, int cityY, int governmentSize, int cityMult, int suburbsMult) {
        this.cityX = cityX;
        this.cityY = cityY;
        this.governmentSize = governmentSize;
        this.cityMult = cityMult;
        this.suburbsMult = suburbsMult;
        setGovernmentSize(governmentSize);
    }

    public void setCenter(int x, int y) {
        this.cityX = x;
        this.cityY = y;
    }

    public void setGovernmentSize(int governmentSize) {
        this.governmentSize = governmentSize;
        this.areas = new CityArea[]{
                new CityArea(this.governmentSize, GOVERNMENT),
                new CityArea(this.governmentSize * cityMult, CITY),
                new CityArea(this.governmentSize * suburbsMult, SUBURB)
        };
    }

    public CityArea getArea(int x, int y) {
        for (CityArea area : areas) {
            if (Math.abs(x - cityX) <= area.getSize() && Math.abs(y - cityY) <= area.getSize()) {
                return area;
            }
        }
        return null;
    }

    public CityArea getArea(Location loc) {
        return getArea(loc.getBlockX(), loc.getBlockZ());
    }

    public void updateDB() {
        try {
            var stmt = SQLConn.getConnection()
                    .prepareStatement("REPLACE INTO cityInfo (size, x, y) VALUE (?, ?, ?);");
            stmt.setInt(1, governmentSize);
            stmt.setInt(2, cityX);
            stmt.setInt(3, cityY);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*public double distToCity(int x, int y) {
        var x_dist = cityX - x;
        var y_dist = cityY - y;
        return Math.sqrt(x_dist * x_dist + y_dist * y_dist);
    }*/

    public boolean inCity(int x, int y) {
        var extents = governmentSize * cityMult;
        return Math.abs(x - cityX) <= extents && Math.abs(y - cityY) <= extents;
    }

    public boolean inSuburbs(int x, int y) {
        var extents = governmentSize * suburbsMult;
        return Math.abs(x - cityX) <= extents && Math.abs(y - cityY) <= extents;
    }

    public boolean inCity(Location loc) {
        return inCity(loc.getBlockX(), loc.getBlockZ());
    }

    public boolean canBreak(Player player, int x, int y) {
        if (!inSuburbs(x, y)) {
            return true;
        }
        var title = RomePlugin.onlinePlayerTitles.get(player);
        var area = getArea(x, y);
        if (area.getType() == SUBURB) {
            var claim = SQLConn.getClaim(x, y);
            if (claim == null) {
                return true;
            }
            return claim.owner.equals(player.getUniqueId());
        }
        if (area.getType().canBuild(title)) {
            return true;
        }
        var claim = SQLConn.getClaim(x, y);
        return claim != null && claim.owner.equals(player.getUniqueId());
    }

    private static boolean rectIntersects(int x0, int y0, int x1, int y1,
                                          int x2, int y2, int x3, int y3) {
        return x0 <= x3 && x1 >= x2 && y0 >= y3 && y1 <= y2;
    }

    private static boolean rectInside(int x0, int y0, int x1, int y1,
                                          int x2, int y2, int x3, int y3) {
        return x2 >= x0 && x3 <= x1 && y2 <= y0 && y3 >= y1;
    }

    public boolean canClaim(Player player, int x0, int y0, int x1, int y1) {
        var extents = governmentSize * suburbsMult;
        if (!rectInside(cityX - extents, cityY + extents, cityX + extents, cityY - extents, x0, y0, x1, y1)) {
            player.sendMessage("you cannot claim outside of city limits");
            return false;
        }
        extents = governmentSize;
        if (rectIntersects(x0, y0, x1, y1, cityX - extents, cityY + extents, cityX + extents, cityY - extents)) {
            player.sendMessage("you cannot claim in government");
            return false;
        }
        extents = governmentSize * cityMult;
        if (rectIntersects(x0, y0, x1, y1, cityX - extents, cityY + extents, cityX + extents, cityY - extents)) {
            var title = SQLConn.getTitle(player.getUniqueId());
            if (title == null || title.t != Title.MAYOR) {
                player.sendMessage("sina ken ala jo e ma ni, sina wawa ala");
                return false;
            }
            return true;
        }
        return SQLConn.getTotalClaimedBlocks(player.getUniqueId()) <= 225;
    }

    public boolean tryClaimLand(Player player, int xa, int ya, int xb, int yb) {
        // ensure x0, y0 is the top left point and x1, y1 is the bottom right point
        var x0 = Math.min(xa, xb);
        var y0 = Math.max(ya, yb);
        var x1 = Math.max(xa, xb);
        var y1 = Math.min(ya, yb);
        if (!canClaim(player, x0, y0, x1, y1)) {
            return false;
        }
        var claim = SQLConn.getClaimRect(x0, y0, x1, y1);
        if (claim != null) {
            player.sendMessage("land already claimed >:(");
            return false;
        }
        SQLConn.addClaim(x0, y0, x1, y1, player.getUniqueId());
        player.sendMessage("successfully claimed " + (x1 - x0 + 1) * (y0 - y1 + 1) + " blocks.");
        return true;
    }

    public boolean canBreak(Player player, Location loc) {
        return canBreak(player, loc.getBlockX(), loc.getBlockZ());
    }
}
