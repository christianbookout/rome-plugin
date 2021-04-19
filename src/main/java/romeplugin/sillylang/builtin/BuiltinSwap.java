package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.SillyType;

import java.util.Deque;
import java.util.Map;

public class BuiltinSwap extends Builtin {

    @Override
    protected String getId() {
        return "__swap";
    }

    @Override
    public void execute(Deque<SillyType> stack) {
        SillyType top = stack.pop();
        SillyType bottom = stack.pop();
        stack.push(top);
        stack.push(bottom);
    }
}
