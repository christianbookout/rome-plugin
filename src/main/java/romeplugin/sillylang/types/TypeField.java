package romeplugin.sillylang.types;

import java.lang.reflect.Field;

public class TypeField implements SillyType {
    Field field;

    public TypeField(TypeClass typeClass, TypeU16Array fieldname) throws NoSuchFieldException {
        field = typeClass.getTypeClass().getField(fieldname.str);
    }

    private TypeField(Field val) {
        field = val;
    }

    @Override
    public SillyType copy() {
        return new TypeField(field);
    }

    @Override
    public Type getType() {
        return Type.FIELD;
    }

    @Override
    public Field getValue() {
        return field;
    }
}
