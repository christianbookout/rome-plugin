package romeplugin.zoning.claims;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import romeplugin.RomePlugin;
import romeplugin.database.SQLConn;
import romeplugin.newtitle.Title;
import romeplugin.zoning.CityArea;

import java.sql.SQLException;

import static romeplugin.zoning.ZoneType.*;

public class LandControl {

    private CityArea[] areas;
    private int cityX, cityY;
    private int governmentSize;
    private final int cityMult;
    private final int suburbsMult;
    private final int minBlockLimit;

    public LandControl(int cityX, int cityY, int governmentSize, int cityMult, int suburbsMult, int minBlockLimit) {
        this.minBlockLimit = minBlockLimit;
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

    private CityArea getArea(int x, int y) {
        for (CityArea area : areas) {
            if (Math.abs(x - cityX) <= area.getSize() && Math.abs(y - cityY) <= area.getSize()) {
                return area;
            }
        }
        return null;
    }

    public CityArea getArea(Location loc) {
        if (loc.getWorld().getEnvironment() != Environment.NORMAL) return null;
        return getArea(loc.getBlockX(), loc.getBlockZ());
    }

    public void updateDB() {
        try {
            var stmt = SQLConn.getConnection()
                    .prepareStatement("REPLACE INTO cityInfo VALUES (0, ?, ?, ?);");
            stmt.setInt(1, governmentSize);
            stmt.setInt(2, cityX);
            stmt.setInt(3, cityY);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean inCity(int x, int y) {
        var extents = governmentSize * cityMult;
        return Math.abs(x - cityX) <= extents && Math.abs(y - cityY) <= extents;
    }

    private boolean inSuburbs(int x, int y) {
        var extents = governmentSize * suburbsMult;
        return Math.abs(x - cityX) <= extents && Math.abs(y - cityY) <= extents;
    }
    public boolean inSuburbs(Location loc) {
        if (loc.getWorld().getEnvironment() != Environment.NORMAL) return false;
        return inSuburbs(loc.getBlockX(), loc.getBlockZ());
    }

    public boolean inCity(Location loc) {
        if (loc.getWorld().getEnvironment() != Environment.NORMAL) return false;
        return inCity(loc.getBlockX(), loc.getBlockZ());
    }

    private boolean canBreak(Player player, int x, int y) {
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
    //TODO make it so you cant claim in da nether and stuff
    private boolean canClaim(Player player, int x0, int y0, int x1, int y1) {
        var extents = governmentSize * suburbsMult;
        if (!rectInside(cityX - extents, cityY + extents, cityX + extents, cityY - extents, x0, y0, x1, y1)) {
            player.sendMessage("you cannot claim outside of city limits");
            return false;
        }
        var title = SQLConn.getTitle(player.getUniqueId());
        if (title != null && title.t == Title.AEDILE) {
            // this allows the mayor to skip the claiming limit check anywhere inside rome
            return true;
        }
        extents = governmentSize;
        if (rectIntersects(x0, y0, x1, y1, cityX - extents, cityY + extents, cityX + extents, cityY - extents)) {
            player.sendMessage("you cannot claim in government");
            return false;
        }
        extents = governmentSize * cityMult;
        if (rectIntersects(x0, y0, x1, y1, cityX - extents, cityY + extents, cityX + extents, cityY - extents)) {
            player.sendMessage("sina ken ala jo e ma ni, sina wawa ala");
            return false;
        }
        var claimed = (x1 - x0) * (y0 - y1);
        if (SQLConn.getTotalClaimedBlocks(player.getUniqueId()) + claimed <= minBlockLimit + SQLConn.getClaimAmount(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "you have hit your block limit!");
            return false;
        }
        return true;
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
        if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {
            return true;
        }
        return canBreak(player, loc.getBlockX(), loc.getBlockZ());
    }

    public boolean expandGovernment(int size) {
        if (governmentSize + size < 0) {
            return false;
        }
        setGovernmentSize(governmentSize + size);
        return true;
    }

    public boolean inWilderness(Location toLoc) {
        return !inSuburbs(toLoc);
    }
}
