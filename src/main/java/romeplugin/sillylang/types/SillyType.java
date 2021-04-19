package romeplugin.sillylang.types;

public interface SillyType {
    enum Type {
        I8,
        I16,
        I32,
        I64,
        F32,
        F64,
        U16,
        U16ARRAY,
    }
    SillyType copy();
    Type getType();
}
