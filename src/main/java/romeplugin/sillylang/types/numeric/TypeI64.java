package romeplugin.sillylang.types.numeric;

import romeplugin.sillylang.types.SillyType;

public class TypeI64 extends TypeInteger {
    private long i64;

    public TypeI64(long val) {
        i64 = val;
    }

    @Override
    public byte asByte() {
        return (byte) i64;
    }

    @Override
    public short asShort() {
        return (short) i64;
    }

    @Override
    public int asInt() {
        return (int) i64;
    }

    @Override
    public long asLong() {
        return i64;
    }

    @Override
    public float asFloat() {
        return i64;
    }

    @Override
    public SillyType copy() {
        return new TypeI64(i64);
    }

    @Override
    public Type getType() {
        return Type.I64;
    }

    @Override
    public Object getValue() {
        return i64;
    }
}
