package romeplugin.sillylang;

import romeplugin.sillylang.builtin.Builtin;
import romeplugin.sillylang.builtin.BuiltinPushType;
import romeplugin.sillylang.types.SillyType;

import java.util.ArrayList;
import java.util.Deque;

public class Subroutine {
    private final ArrayList<Builtin> instructions;

    public Subroutine() {
        instructions = new ArrayList<>();
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
}
