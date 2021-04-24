package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.SillyType;

import java.util.Deque;

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
    public void execute(Deque<SillyType> stack) {
        throw new UnsupportedOperationException();
    }

    public String getIdentifier() {
        return identifier;
    }
}
