package romeplugin.zoning;

public class Square {
    private final int size;
    private final ZoneType type;

    public Square(int size, ZoneType type) {
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
