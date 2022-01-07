package romeplugin.database;

import java.util.UUID;

public class ClaimEntry {
    public final int x0;
    public final int y0;
    public final int x1;
    public final int y1;
    public final UUID owner;

    ClaimEntry(int x0, int y0, int x1, int y1, UUID owner) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.owner = owner;
    }

    public int getArea() {
        return (x1 - x0 + 1) * (y0 - y1 + 1);
    }
}
