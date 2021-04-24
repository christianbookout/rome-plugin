package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.SillyType;

import java.util.Deque;
import java.util.HashMap;

public class BuiltinDuplicate extends Builtin {

    @Override
    protected String getId() {
        return "__duplicate";
    }

    @Override
    public void execute(Deque<SillyType> stack, HashMap<String, SillyType> values) {
        SillyType object = stack.pop();
        stack.push(object);
        stack.push(object.copy());
    }
}
