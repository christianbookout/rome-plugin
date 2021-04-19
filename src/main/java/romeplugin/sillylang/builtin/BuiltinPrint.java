package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.SillyType;

import java.util.Deque;
import java.util.Map;

public class BuiltinPrint extends Builtin {
    public BuiltinPrint(Map<String, Builtin> builtins) {
        super(builtins);
    }

    @Override
    protected String getId() {
        return "__print";
    }

    @Override
    public void execute(Deque<SillyType> stack) {
        System.out.println(stack.pop());
    }
}
