package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.SillyType;

import java.util.Deque;

public class BuiltinDelete extends Builtin {
    @Override
    protected String getId() {
        return "__delete";
    }

    @Override
    public void execute(Deque<SillyType> stack) {
        stack.pop();
    }
}
