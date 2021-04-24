package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.SillyType;
import romeplugin.sillylang.types.TypeJava;
import romeplugin.sillylang.types.TypeMethod;
import romeplugin.sillylang.types.TypeNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Deque;
import java.util.HashMap;

public class BuiltinExecute extends Builtin {
    @Override
    protected String getId() {
        return "__execute";
    }

    @Override
    public void execute(Deque<SillyType> stack, HashMap<String, SillyType> values) {
        TypeMethod method = (TypeMethod) stack.pop();
        SillyType object = stack.pop();
        Object[] args = new Object[method.parameterCount()];
        for (int i = 0; i < method.parameterCount(); i++) {
            args[i] = stack.pop().getValue();
        }
        try {
            Object result = method.getValue().invoke(object.getValue(), args);
            if (result == null) {
                stack.push(new TypeNull());
            } else {
                stack.push(new TypeJava(result));
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
