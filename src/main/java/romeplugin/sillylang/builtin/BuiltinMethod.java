package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.*;
import romeplugin.sillylang.types.numeric.TypeInteger;

import java.util.Deque;
import java.util.HashMap;

public class BuiltinMethod extends Builtin {
    @Override
    protected String getId() {
        return "__method";
    }

    @Override
    public void execute(Deque<SillyType> stack, HashMap<String, SillyType> values) {
        try {
            TypeClass typeClass = (TypeClass) stack.pop();
            TypeU16Array name = (TypeU16Array) stack.pop();
            TypeInteger count = (TypeInteger) stack.pop();
            Class<?>[] params = new Class<?>[count.asInt()];
            for (int i = 0; i < count.asInt(); i++) {
                params[i] = (Class<?>) stack.pop().getValue();
            }
            stack.push(new TypeMethod(typeClass, name, params));
        } catch (NoSuchMethodException e) {
            stack.push(new TypeNull());
            e.printStackTrace();
        }
    }
}
