package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.SillyType;

import java.util.Deque;
import java.util.HashMap;

public class BuiltinPushType extends Builtin {
    private final SillyType obj;

    public BuiltinPushType(SillyType obj) {
        this.obj = obj;
    }

    @Override
    protected String getId() {
        return null;
    }

    @Override
    public void execute(Deque<SillyType> stack, HashMap<String, SillyType> values) {
        stack.push(obj);
    }
}
