package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.SillyType;
import romeplugin.sillylang.types.TypeField;
import romeplugin.sillylang.types.TypeJava;

import java.util.Deque;
import java.util.HashMap;

public class BuiltinFieldGet extends Builtin {
    @Override
    protected String getId() {
        return "__field_get";
    }

    @Override
    public void execute(Deque<SillyType> stack, HashMap<String, SillyType> values) {
        TypeField field = (TypeField) stack.pop();
        try {
            stack.push(new TypeJava(field.getValue().get(stack.pop().getValue())));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
