package romeplugin.sillylang.types;

public class TypeU16Array implements SillyType {
    String str;

    public TypeU16Array(String val) {
        str = val;
    }

    @Override
    public SillyType copy() {
        return new TypeU16Array(str);
    }

    @Override
    public Type getType() {
        return Type.U16ARRAY;
    }

    @Override
    public Object getValue() {
        return str;
    }

    @Override
    public String toString() {
        return str;
    }
}
