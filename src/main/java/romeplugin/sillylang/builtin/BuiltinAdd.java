package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.SillyType;
import romeplugin.sillylang.types.numeric.*;

import java.util.Deque;
import java.util.Map;

public class BuiltinAdd extends Builtin {
    public BuiltinAdd(Map<String, Builtin> builtins) {
        super(builtins);
    }

    @Override
    protected String getId() {
        return "__add";
    }

    @Override
    public void execute(Deque<SillyType> stack) {
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
                // TODO: f64
                break;
        }
    }
}
