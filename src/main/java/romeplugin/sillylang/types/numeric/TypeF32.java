package romeplugin.sillylang.types.numeric;

import romeplugin.sillylang.types.SillyType;

public class TypeF32 implements TypeNumeric {
    public float f32;

    public TypeF32(float val) {
        f32 = val;
    }

    @Override
    public SillyType copy() {
        return new TypeF32(f32);
    }

    @Override
    public SillyType.Type getType() {
        return SillyType.Type.F32;
    }

    @Override
    public Object getValue() {
        return f32;
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
        return (byte) f32;
    }

    @Override
    public short asShort() {
        return (short) f32;
    }

    @Override
    public int asInt() {
        return (int) f32;
    }

    @Override
    public long asLong() {
        return (long) f32;
    }

    @Override
    public float asFloat() {
        return f32;
    }

    @Override
    public double asDouble() {
        return f32;
    }
}
