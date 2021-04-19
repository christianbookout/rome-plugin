package romeplugin.sillylang.types;

public class TypeClass implements SillyType {
    private final Class<?> aClass;

    public TypeClass(TypeU16Array name) throws ClassNotFoundException {
        aClass = Class.forName(name.str);
    }

    private TypeClass(Class<?> aClass) {
        this.aClass = aClass;
    }

    public Class<?> getTypeClass() {
        return aClass;
    }


    @Override
    public SillyType copy() {
        return new TypeClass(aClass);
    }

    @Override
    public Type getType() {
        return Type.CLASS;
    }

    @Override
    public Object getValue() {
        return aClass;
    }
}
