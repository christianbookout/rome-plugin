package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.*;

import java.util.Deque;

public class BuiltinField extends Builtin {
    @Override
    protected String getId() {
        return "__field";
    }

    @Override
    public void execute(Deque<SillyType> stack) {
        try {
            stack.push(new TypeField((TypeClass) stack.pop(), (TypeU16Array) stack.pop()));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
