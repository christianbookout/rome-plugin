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
        NULL,
        CLASS,
        METHOD,
        FIELD,
        JAVA
    }
    SillyType copy();
    Type getType();
    Object getValue();
}
