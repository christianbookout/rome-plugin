package romeplugin.sillylang;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiConsumer;

public class SillyProgram {
    private final HashMap<String, Subroutine> subroutines;
    private final HashSet<String> values;

    public SillyProgram() {
        subroutines = new HashMap<>();
        values = new HashSet<>();
    }

    public void addRoutine(String subroutineName, Subroutine parseSubroutine) {
        subroutines.put(subroutineName, parseSubroutine);
    }

    public HashMap<String, Subroutine> getRoutines() {
        return subroutines;
    }

    public Subroutine getRoutine(String name) {
        return subroutines.get(name);
    }

    public void addValue(String identifier) {
        values.add(identifier);
    }

    public void forEachRoutine(BiConsumer<String, Subroutine> consumer) {
        subroutines.forEach(consumer);
    }
}
