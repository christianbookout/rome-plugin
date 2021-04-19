package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.SillyType;

import java.util.Deque;
import java.util.Map;

abstract public class Builtin {
    public void register(Map<String, Builtin> builtins) {
        builtins.put(getId(), this);
    }
    protected abstract String getId();
    public abstract void execute(Deque<SillyType> stack);
}
