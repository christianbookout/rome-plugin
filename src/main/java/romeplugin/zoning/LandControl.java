package romeplugin.zoning;

import org.bukkit.entity.Player;
import romeplugin.RomePlugin;
import romeplugin.database.SQLConn;

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

    public CityArea getRing(int x, int y) {
        for (CityArea area : areas) {
            if (Math.abs(x) <= area.getSize() + cityX && Math.abs(y) <= area.getSize() + cityY) {
                return area;
            }
        }
        return null;
    }

    /*public double distToCity(int x, int y) {
        var x_dist = cityX - x;
        var y_dist = cityY - y;
        return Math.sqrt(x_dist * x_dist + y_dist * y_dist);
    }*/

    private boolean inCity(int x, int y) {
        var extents = governmentSize * suburbsMult;
        return Math.abs(x - cityX) <= extents && Math.abs(y - cityY) <= extents;
    }

    public boolean canBreak(Player player, int x, int y) {
        if (!inCity(x, y)) {
            return true;
        }
        var title = RomePlugin.onlinePlayerTitles.get(player);
        if (getRing(x, y).getType().canBuild(title)) {
            return true;
        }
        var claim = SQLConn.getClaim(x, y);
        return claim != null && claim.owner == player.getUniqueId();
    }
}
