package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.SillyType;

import java.util.Deque;

public class BuiltinPrint extends Builtin {

    @Override
    protected String getId() {
        return "__print";
    }

    @Override
    public void execute(Deque<SillyType> stack) {
        System.out.println(stack.pop().getValue());
    }
}
