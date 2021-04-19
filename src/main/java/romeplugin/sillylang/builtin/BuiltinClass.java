package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.SillyType;
import romeplugin.sillylang.types.TypeClass;
import romeplugin.sillylang.types.TypeU16Array;

import java.util.Deque;

public class BuiltinClass extends Builtin {
    @Override
    protected String getId() {
        return "__class";
    }

    @Override
    public void execute(Deque<SillyType> stack) {
        try {
            stack.push(new TypeClass((TypeU16Array) stack.pop()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
