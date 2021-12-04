package romeplugin.zoning;

import org.bukkit.entity.Player;
import romeplugin.RomePlugin;

import static romeplugin.zoning.ZoneType.*;

public class LandControl {

    private CityArea[] areas;
    private int cityX, cityY;
    private int initialSize;
    private final int cityMult;
    private final int suburbsMult;

    public LandControl(int cityX, int cityY, int initialSize, int cityMult, int suburbsMult) {
        this.cityX = cityX;
        this.cityY = cityY;
        this.initialSize = initialSize;
        this.cityMult = cityMult;
        this.suburbsMult = suburbsMult;
        this.areas = new CityArea[]{
                new CityArea(initialSize, GOVERNMENT),
                new CityArea(initialSize * cityMult, CITY),
                new CityArea(initialSize * suburbsMult, SUBURB)
        };
    }

    public void setCenter(int x, int y) {
        this.cityX = x;
        this.cityY = y;
    }

    public void setCitySize(int citySize) {
        this.initialSize = citySize;
        this.areas = new CityArea[]{
                new CityArea(initialSize, GOVERNMENT),
                new CityArea(initialSize * cityMult, CITY),
                new CityArea(initialSize * suburbsMult, SUBURB)
        };
    }

    public CityArea getRing(int x, int y) {
        for (CityArea area : areas) {
            if (x <= area.getSize() + cityX && y <= area.getSize() + cityY) {
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
        var extents = initialSize * suburbsMult;
        return Math.abs(x - cityX) <= extents && Math.abs(y - cityY) <= extents;
    }

    public boolean canBreak(Player player, int x, int y) { //TODO implement a player cache
        if (!inCity(x, y)) {
            return true;
        }
        var title = RomePlugin.onlinePlayers.get(player);
        if (title == null) {
            return false;
        }
        return getRing(x, y).getType().canBuild(title);
    }
}
