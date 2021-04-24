package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.SillyType;

import java.util.Deque;
import java.util.HashMap;

public class BuiltinDelete extends Builtin {
    @Override
    protected String getId() {
        return "__delete";
    }

    @Override
    public void execute(Deque<SillyType> stack, HashMap<String, SillyType> values) {
        stack.pop();
    }
}
