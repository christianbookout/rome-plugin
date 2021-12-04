package romeplugin.zoning;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.entity.Player;

import romeplugin.RomePlugin;
import romeplugin.database.SQLConn;
import romeplugin.newtitle.Title;

import static romeplugin.zoning.ZoneType.*;

public class LandControl {

    private Square[] rings;
    private int cityX, cityY;
    private int initialSize;

    public LandControl(int cityX, int cityY, int initialSize, int cityMult, int suburbsMult) {
        this.cityX= cityX;
        this.cityY = cityY;
        this.initialSize = initialSize;
        this.rings = new Square[] {
            new Square(initialSize, GOVERNMENT),
            new Square(initialSize*cityMult, CITY),
            new Square(initialSize*suburbsMult, SUBURB)
        };
    }

    public void setCenter(int x, int y) {
        this.cityX = x;
        this.cityY = y;
    }

    /*public void setCitySize(int citySize) {
        this.initialSize = citySize;
        //TODOLATER :)
    }*/

    public Square getRing(int x, int y) {
        for (Square square : rings) {
            if (x <= square.getSize() + cityX && y <= square.getSize() + cityY) return square;
        }
        return null;
    }

    /*public double distToCity(int x, int y) {
        var x_dist = cityX - x;
        var y_dist = cityY - y;
        return Math.sqrt(x_dist * x_dist + y_dist * y_dist);
    }*/

    public boolean canBreak(Player player, int x, int y) { //TODO implement a player cache
        Title title;
        if ((title = RomePlugin.onlinePlayers.get(player)) == null) return false;
        return getRing(x, y).getType().canBuild(title);
    }
}
