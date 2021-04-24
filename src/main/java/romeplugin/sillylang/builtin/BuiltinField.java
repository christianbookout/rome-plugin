package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.*;

import java.util.Deque;
import java.util.HashMap;

public class BuiltinField extends Builtin {
    @Override
    protected String getId() {
        return "__field";
    }

    @Override
    public void execute(Deque<SillyType> stack, HashMap<String, SillyType> values) {
        try {
            stack.push(new TypeField((TypeClass) stack.pop(), (TypeU16Array) stack.pop()));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
