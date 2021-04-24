package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.SillyType;

import java.util.Deque;

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
    public void execute(Deque<SillyType> stack) {
        stack.push(obj);
    }
}
