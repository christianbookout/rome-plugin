package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.SillyType;
import romeplugin.sillylang.types.TypeField;
import romeplugin.sillylang.types.TypeJava;

import java.util.Deque;

public class BuiltinFieldGet extends Builtin {
    @Override
    protected String getId() {
        return "__field_get";
    }

    @Override
    public void execute(Deque<SillyType> stack) {
        TypeField field = (TypeField) stack.pop();
        try {
            stack.push(new TypeJava(field.getValue().get(stack.pop().getValue())));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
