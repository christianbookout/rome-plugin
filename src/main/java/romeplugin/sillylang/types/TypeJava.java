package romeplugin.sillylang.types;

public class TypeJava implements SillyType {
    private Object java;

    public TypeJava(Object val) {
        java = val;
    }

    @Override
    public SillyType copy() {
        return new TypeJava(java);
    }

    @Override
    public Type getType() {
        return Type.JAVA;
    }

    @Override
    public Object getValue() {
        return java;
    }
}
