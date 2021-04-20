package romeplugin.sillylang.types.numeric;

import romeplugin.sillylang.types.SillyType;

public interface TypeNumeric extends SillyType {
    boolean isFloat();
    boolean isInteger();

    byte asByte();
    short asShort();
    int asInt();
    long asLong();

    float asFloat();
    double asDouble();
}
