package romeplugin.sillylang;

import romeplugin.sillylang.builtin.Builtin;
import romeplugin.sillylang.builtin.BuiltinIdentifier;
import romeplugin.sillylang.builtin.BuiltinPushType;
import romeplugin.sillylang.types.SillyType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class Subroutine {
    public static final byte FLAG_NONE = 0;
    public static final byte FLAG_SPIGOT_COMMAND = 0b00000001;

    private final Deque<Builtin> instructions;
    public byte flags;

    public Subroutine(byte flags) {
        instructions = new ArrayDeque<>();
        this.flags = flags;
    }

    public Subroutine() {
        this(FLAG_NONE);
    }

    public void add(Builtin instruction) {
        instructions.add(instruction);
    }

    public void addType(SillyType type) {
        instructions.add(new BuiltinPushType(type));
    }

    public void execute(Deque<SillyType> stack) {
        for (Builtin instr : instructions) {
            instr.execute(stack);
        }
    }

    public void addIdentifier(String identifier) {
        instructions.add(new BuiltinIdentifier(identifier));
    }

    public Builtin pop() {
        return instructions.pop();
    }
}
