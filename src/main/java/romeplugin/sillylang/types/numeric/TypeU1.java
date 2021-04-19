package romeplugin.sillylang.types.numeric;

import romeplugin.sillylang.types.SillyType;

public class TypeU1 extends TypeInteger {
    private final boolean u1;

    public TypeU1(boolean u1) {
        this.u1 = u1;
    }

    @Override
    public byte asByte() {
        return (byte) (u1 ? 1 : 0);
    }

    @Override
    public short asShort() {
        return (short) (u1 ? 1 : 0);
    }

    @Override
    public int asInt() {
        return u1 ? 1 : 0;
    }

    @Override
    public long asLong() {
        return u1 ? 1L : 0L;
    }

    @Override
    public float asFloat() {
        return 0;
    }

    @Override
    public SillyType copy() {
        return new TypeU1(u1);
    }

    @Override
    public Type getType() {
        return Type.U1;
    }

    @Override
    public Object getValue() {
        return u1;
    }
}
