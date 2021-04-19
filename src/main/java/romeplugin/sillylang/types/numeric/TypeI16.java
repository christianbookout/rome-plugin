package romeplugin.sillylang.types.numeric;

import romeplugin.sillylang.types.SillyType;

public class TypeI16 extends TypeInteger {
    public short i16;

    public TypeI16(short val) {
        i16 = val;
    }

    @Override
    public SillyType copy() {
        return new TypeI16(i16);
    }

    @Override
    public Type getType() {
        return Type.I16;
    }

    @Override
    public Object getValue() {
        return i16;
    }

    @Override
    public byte asByte() {
        return (byte) i16;
    }

    @Override
    public short asShort() {
        return i16;
    }

    @Override
    public int asInt() {
        return i16;
    }

    @Override
    public long asLong() {
        return i16;
    }

    @Override
    public float asFloat() {
        return i16;
    }
}
