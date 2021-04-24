package romeplugin.sillylang.builtin;

import romeplugin.sillylang.Subroutine;
import romeplugin.sillylang.types.SillyType;

import java.util.Deque;
import java.util.HashMap;

public class BuiltinValueGet extends Builtin {
    private String valueName;
    @Override
    protected String getId() {
        return "__value_get";
    }

    @Override
    public void compileExecute(Subroutine routine) {
        BuiltinIdentifier identifier = (BuiltinIdentifier) routine.pop();
        valueName = identifier.getIdentifier();
        super.compileExecute(routine);
    }

    @Override
    public void execute(Deque<SillyType> stack, HashMap<String, SillyType> values) {
        stack.push(values.get(valueName));
    }
}
