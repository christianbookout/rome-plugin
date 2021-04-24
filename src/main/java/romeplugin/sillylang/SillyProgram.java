package romeplugin.sillylang;

import java.util.HashMap;

public class SillyProgram {
    private final HashMap<String, Subroutine> subroutines;

    public SillyProgram() {
        subroutines = new HashMap<>();
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
}
