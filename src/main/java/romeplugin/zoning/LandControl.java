package romeplugin.zoning;

import java.util.Optional;

public class LandControl {
    private int cityX = 0;
    private int cityY = 0;
    private int citySize = 0;

    // TODO: don't hardcode this
    private final Ring[] rings = {
            new Ring(10, "A"),
            new Ring(20, "B")
    };

    public void setCenter(int x, int y) {
        this.cityX = x;
        this.cityY = y;
    }

    public void setCitySize(int citySize) {
        this.citySize = citySize;
    }

    public Ring getRing(double dist) {
        // TODO: figure out who can break blocks in a ring
        for (Ring ring : rings) {
            if (ring.blocksFromCenter < dist) {
                return ring;
            }
        }
        return null;
    }

    public double distToCity(int x, int y) {
        var x_dist = cityX - x;
        var y_dist = cityY - y;
        return Math.sqrt(x_dist * x_dist + y_dist * y_dist);
    }

    public boolean canBreak(int x, int y) {
        var dist = distToCity(x, y);
        if (dist > citySize) {
            return true;
        }
        return getRing(dist).name.equals("B");
    }
}
