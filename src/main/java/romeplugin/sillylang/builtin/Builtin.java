package romeplugin.sillylang.builtin;

import romeplugin.sillylang.Subroutine;
import romeplugin.sillylang.types.SillyType;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

abstract public class Builtin {
    public void register(Map<String, Builtin> builtins) {
        builtins.put(getId(), this);
    }

    protected abstract String getId();

    // compile-time optimizations
    public void compileExecute(Subroutine routine) {
        routine.add(this);
    }

    // runtime execution
    public abstract void execute(Deque<SillyType> stack, HashMap<String, SillyType> values);
}
