package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.SillyType;
import romeplugin.sillylang.types.numeric.*;

import java.util.Deque;
import java.util.HashMap;

public class BuiltinAdd extends Builtin {
    @Override
    protected String getId() {
        return "__add";
    }

    @Override
    public void execute(Deque<SillyType> stack, HashMap<String, SillyType> values) {
        // TODO: addition
        TypeNumeric first = (TypeNumeric) stack.pop();
        TypeNumeric second = (TypeNumeric) stack.pop();
        switch (second.getType()) {
            case I8:
                stack.push(new TypeI8((byte) (second.asByte() + first.asLong())));
                break;
            case I16:
                stack.push(new TypeI16((short) (second.asShort() + first.asLong())));
                break;
            case I32:
                stack.push(new TypeI32(second.asInt() + first.asInt()));
                break;
            case I64:
                stack.push(new TypeI64(second.asLong() + first.asLong()));
                break;
            case F32:
                stack.push(new TypeF32(second.asFloat() + first.asFloat()));
                break;
            case F64:
                stack.push(new TypeF64(second.asDouble() + first.asDouble()));
                break;
        }
    }
}
