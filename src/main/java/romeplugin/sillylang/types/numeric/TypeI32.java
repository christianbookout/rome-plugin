package romeplugin.sillylang.types.numeric;

import romeplugin.sillylang.types.SillyType;

public class TypeI32 extends TypeInteger {
    public int i32;

    public TypeI32(int val) {
        i32 = val;
    }

    @Override
    public byte asByte() {
        return (byte) i32;
    }

    @Override
    public short asShort() {
        return (short) i32;
    }

    @Override
    public int asInt() {
        return i32;
    }

    @Override
    public long asLong() {
        return i32;
    }

    @Override
    public float asFloat() {
        return i32;
    }

    @Override
    public SillyType copy() {
        return new TypeI32(i32);
    }

    @Override
    public Type getType() {
        return Type.I32;
    }

    @Override
    public Object getValue() {
        return i32;
    }

    @Override
    public String toString() {
        return "TypeI32{" +
                "i32=" + i32 +
                '}';
    }
}
