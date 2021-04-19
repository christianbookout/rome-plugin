package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.SillyType;

import java.util.Deque;
import java.util.Map;

public class BuiltinDuplicate extends Builtin {

    @Override
    protected String getId() {
        return "__duplicate";
    }

    @Override
    public void execute(Deque<SillyType> stack) {
        SillyType object = stack.pop();
        stack.push(object);
        stack.push(object.copy());
    }
}
