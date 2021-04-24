package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.SillyType;

import java.util.Deque;
import java.util.HashMap;

public class BuiltinIdentifier extends Builtin {
    private final String identifier;

    public BuiltinIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    protected String getId() {
        return null;
    }

    @Override
    public void execute(Deque<SillyType> stack, HashMap<String, SillyType> values) {
        throw new UnsupportedOperationException();
    }

    public String getIdentifier() {
        return identifier;
    }
}
