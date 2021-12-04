package romeplugin.zoning;

public class CityArea {
    private final int size;
    private final ZoneType type;

    public CityArea(int size, ZoneType type) {
        this.size = size;
        this.type = type;
    }

    public int getSize() {
        return this.size;
    }

    public ZoneType getType() {
        return this.type;
    }
}
