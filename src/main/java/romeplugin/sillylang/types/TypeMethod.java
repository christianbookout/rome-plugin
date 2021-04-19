package romeplugin.sillylang.types;

import java.lang.reflect.Method;

public class TypeMethod implements SillyType {
    Method method;

    public TypeMethod(TypeClass typeClass, TypeU16Array methodName, Class<?>[] ptypes) throws NoSuchMethodException {
        method = typeClass.getTypeClass().getMethod(methodName.str, ptypes);
    }

    private TypeMethod(Method method) {
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    public int parameterCount() {
        return method.getParameterCount();
    }

    @Override
    public TypeMethod copy() {
        return new TypeMethod(method);
    }

    @Override
    public Type getType() {
        return Type.METHOD;
    }

    @Override
    public Method getValue() {
        return method;
    }
}
