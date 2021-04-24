package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.SillyType;

import java.util.Deque;
import java.util.HashMap;

public class BuiltinSwap extends Builtin {

    @Override
    protected String getId() {
        return "__swap";
    }

    @Override
    public void execute(Deque<SillyType> stack, HashMap<String, SillyType> values) {
        SillyType top = stack.pop();
        SillyType bottom = stack.pop();
        stack.push(top);
        stack.push(bottom);
    }
}
