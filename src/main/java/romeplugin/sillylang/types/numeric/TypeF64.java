package romeplugin.sillylang.types.numeric;

import romeplugin.sillylang.types.SillyType;

public class TypeF64 implements TypeNumeric {
    double f64;

    public TypeF64(double val) {
        f64 = val;
    }

    @Override
    public boolean isFloat() {
        return true;
    }

    @Override
    public boolean isInteger() {
        return false;
    }

    @Override
    public byte asByte() {
        return (byte) f64;
    }

    @Override
    public short asShort() {
        return (short) f64;
    }

    @Override
    public int asInt() {
        return (int) f64;
    }

    @Override
    public long asLong() {
        return (long) f64;
    }

    @Override
    public float asFloat() {
        return (float) f64;
    }

    @Override
    public double asDouble() {
        return f64;
    }

    @Override
    public SillyType copy() {
        return new TypeF64(f64);
    }

    @Override
    public Type getType() {
        return Type.F64;
    }

    @Override
    public Object getValue() {
        return f64;
    }
}
