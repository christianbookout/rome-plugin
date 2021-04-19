package romeplugin.sillylang.types;

public class TypeNull implements SillyType {
    @Override
    public SillyType copy() {
        return new TypeNull();
    }

    @Override
    public Type getType() {
        return Type.NULL;
    }

    @Override
    public Object getValue() {
        return null;
    }
}
