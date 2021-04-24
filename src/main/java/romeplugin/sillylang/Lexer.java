package romeplugin.sillylang;

import romeplugin.sillylang.builtin.*;
import romeplugin.sillylang.types.SillyType;
import romeplugin.sillylang.types.TypeU16Array;
import romeplugin.sillylang.types.numeric.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.util.HashMap;

public class Lexer {
    private final HashMap<String, Builtin> builtinMap;
    private static final int BUFFER_SIZE = 512;
    private final byte[] buffer = new byte[BUFFER_SIZE];

    public Lexer() {
        builtinMap = new HashMap<>();
        new BuiltinAdd().register(builtinMap);
        new BuiltinClass().register(builtinMap);
        new BuiltinDelete().register(builtinMap);
        new BuiltinDuplicate().register(builtinMap);
        new BuiltinPrint().register(builtinMap);
        new BuiltinMethod().register(builtinMap);
        new BuiltinExecute().register(builtinMap);
        new BuiltinField().register(builtinMap);
        new BuiltinFieldGet().register(builtinMap);
        new BuiltinSwap().register(builtinMap);
    }

    public Subroutine parseSubroutine(InputStream stream) throws IOException {
        Subroutine routine = new Subroutine();
        Interpreter.LexerState state = Interpreter.LexerState.SEEK_ANY;
        SillyType.Type type = null;

        int buffer_size = 0;
        int c;
        while ((c = stream.read()) != -1) {
            switch (state) {
                case SEEK_ANY:
                    if (Character.isWhitespace(c)) {
                        break;
                    } else if (c == '"') {
                        state = Interpreter.LexerState.READ_STRING;
                    } else {
                        buffer_size = 0;
                        buffer[buffer_size++] = (byte) c;
                        state = Interpreter.LexerState.READ_TYPE;
                    }
                    break;
                case READ_STRING:
                    if (c == '"') {
                        state = Interpreter.LexerState.SEEK_ANY;
                        routine.addType(new TypeU16Array(new String(buffer, 0, buffer_size)));
                        buffer_size = 0;
                    } else if (c == '\\') {
                        // TODO: handle escape characters
                        switch (stream.read()) {
                            case -1:
                                throw new IOException();
                            case '"':
                                buffer[buffer_size++] = '"';
                                break;
                            case 'n':
                                buffer[buffer_size++] = '\n';
                                break;
                            default:
                                System.err.println("DIDN'T IMPLEMENT THIS!");
                                break;
                        }
                    } else {
                        if (buffer_size == BUFFER_SIZE) {
                            throw new BufferOverflowException();
                        }
                        buffer[buffer_size++] = (byte) c;
                    }
                    break;
                case READ_TYPE:
                    if (Character.isWhitespace(c)) {
                        String identifier = new String(buffer, 0, buffer_size);
                        state = Interpreter.LexerState.SEEK_ANY;
                        buffer_size = 0;

                        if (identifier.startsWith("__")) {
                            // System.out.println(identifier);
                            if (identifier.equals("__routine_end")) {
                                return routine;
                            }
                            builtinMap.get(identifier).compileExecute(routine);
                        } else {
                            routine.addIdentifier(identifier);
                        }
                    } else if (c == '$') {
                        String typename = new String(buffer, 0, buffer_size);
                        // FIXME: this is excessively clobbered together
                        type = SillyType.Type.valueOf(typename.toUpperCase());
                        buffer_size = 0;
                        state = Interpreter.LexerState.READ_NUM_LITERAL;
                    } else {
                        buffer[buffer_size++] = (byte) c;
                    }
                    break;
                case READ_NUM_LITERAL:
                    if (Character.isWhitespace(c)) {
                        state = Interpreter.LexerState.SEEK_ANY;
                        String num = new String(buffer, 0, buffer_size);
                        buffer_size = 0;
                        switch (type) {
                            case I8:
                                routine.addType(new TypeI8(Byte.parseByte(num)));
                                break;
                            case I16:
                                routine.addType(new TypeI16(Short.parseShort(num)));
                                break;
                            case I32:
                                routine.addType(new TypeI32(Integer.parseInt(num)));
                                break;
                            case I64:
                                routine.addType(new TypeI64(Long.parseLong(num)));
                                break;
                            case U1:
                                routine.addType(new TypeU1(Byte.parseByte(num) != 0));
                                break;
                            case F32:
                                routine.addType(new TypeF32(Float.parseFloat(num)));
                                break;
                            case F64:
                                routine.addType(new TypeF64(Double.parseDouble(num)));
                                break;
                            default:
                                throw new IOException("unexpected type " + type.name());
                        }
                    } else {
                        buffer[buffer_size++] = (byte) c;
                    }
                    break;
            }
        }
        throw new IOException("unexpected EOF");
    }

    private enum ProgramLexerState {
        READ_IDENTIFIER
    }

    public SillyProgram parseProgram(InputStream stream) throws IOException {
        ProgramLexerState state = ProgramLexerState.READ_IDENTIFIER;
        SillyProgram prog = new SillyProgram();
        String currentIdentifier = null;
        int buffer_size = 0;
        int c;
        while (true) {
            c = stream.read();
            if (c == -1) return prog;
            if (Character.isWhitespace(c)) {
                String identifier = new String(buffer, 0, buffer_size);

                if (identifier.startsWith("__")) {
                    if (currentIdentifier == null) {
                        throw new IOException("identifier expected, not builtin");
                    }
                    // builtin switch or something
                    switch (identifier) {
                        case "__value":
                            prog.addValue(currentIdentifier);
                            break;
                        case "__routine":
                            prog.addRoutine(currentIdentifier, parseSubroutine(stream));
                            break;
                        case "__spigot_command":
                            prog.getRoutine(currentIdentifier).flags |= Subroutine.FLAG_SPIGOT_COMMAND;
                            break;
                    }
                } else {
                    currentIdentifier = identifier;
                }
            } else {
                buffer[buffer_size++] = (byte) c;
            }
        }
    }

}
