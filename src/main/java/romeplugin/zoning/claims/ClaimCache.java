package romeplugin.zoning.claims;

import romeplugin.database.ClaimEntry;
import romeplugin.database.SQLConn;

import java.util.*;

class ClaimCache {
    ClaimCache(int maxItems) {
        this.items = new LinkedHashMap<>(maxItems, 0.75f, false) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Pos, Optional<ClaimEntry>> eldest) {
                return this.size() > maxItems;
            }
        };
    }

    // LRU cache for blocks -> uuid
    private final LinkedHashMap<Pos, Optional<ClaimEntry>> items;

    public Optional<ClaimEntry> getOrQuery(int x, int z) {
        return items.computeIfAbsent(new Pos(x, z), pos -> {
            var claim = SQLConn.getClaim(x, z);
            if (claim == null) {
                return Optional.empty();
            }
            return Optional.of(claim);
        });
    }

    private static class Pos {
        public final int x;
        public final int z;

        private Pos(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pos pos = (Pos) o;
            return x == pos.x &&
                    z == pos.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, z);
        }
    }
}
