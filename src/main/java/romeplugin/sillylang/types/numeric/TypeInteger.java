package romeplugin.sillylang.types.numeric;

public abstract class TypeInteger implements TypeNumeric {
    @Override
    public boolean isFloat() {
        return false;
    }

    @Override
    public boolean isInteger() {
        return true;
    }
}
