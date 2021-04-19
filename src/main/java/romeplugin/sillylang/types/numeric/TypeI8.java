package romeplugin.sillylang.types.numeric;

import romeplugin.sillylang.types.SillyType;

public class TypeI8 extends TypeInteger {
    public byte i8;

    public TypeI8(byte i8) {
        this.i8 = i8;
    }

    @Override
    public SillyType copy() {
        return new TypeI8(i8);
    }

    @Override
    public Type getType() {
        return Type.I8;
    }

    @Override
    public byte asByte() {
        return i8;
    }

    @Override
    public short asShort() {
        return i8;
    }

    @Override
    public int asInt() {
        return i8;
    }

    @Override
    public long asLong() {
        return i8;
    }

    @Override
    public float asFloat() {
        return i8;
    }
}
