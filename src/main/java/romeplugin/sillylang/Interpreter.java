package romeplugin.sillylang;

import romeplugin.sillylang.builtin.*;
import romeplugin.sillylang.types.SillyType;
import romeplugin.sillylang.types.TypeU16Array;
import romeplugin.sillylang.types.numeric.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class Interpreter {
    private final Deque<SillyType> stack;
    private final Map<String, Builtin> builtins;

    public Interpreter() {
        stack = new ArrayDeque<>();
        builtins = new HashMap<>();
        new BuiltinAdd().register(builtins);
        new BuiltinClass().register(builtins);
        new BuiltinDelete().register(builtins);
        new BuiltinDuplicate().register(builtins);
        new BuiltinPrint().register(builtins);
        new BuiltinMethod().register(builtins);
        new BuiltinExecute().register(builtins);
        new BuiltinField().register(builtins);
        new BuiltinFieldGet().register(builtins);
    }

    private enum LexerState {
        SEEK_ANY,
        READ_STRING,
        READ_TYPE,
        READ_NUM_LITERAL,
    }

    private final int BUFFER_SIZE = 1024;

    public void interpret(InputStream stream) {

        try {
            LexerState state = LexerState.SEEK_ANY;
            SillyType.Type type = null;
            byte[] buffer = new byte[BUFFER_SIZE];
            int buffer_size = 0;
            int c;
            while ((c = stream.read()) != -1) {
                switch (state) {
                    case SEEK_ANY:
                        if (Character.isWhitespace(c)) {
                            break;
                        } else if (c == '"') {
                            state = LexerState.READ_STRING;
                        } else {
                            buffer_size = 0;
                            buffer[buffer_size++] = (byte) c;
                            state = LexerState.READ_TYPE;
                        }
                        break;
                    case READ_STRING:
                        if (c == '"') {
                            state = LexerState.SEEK_ANY;
                            stack.push(new TypeU16Array(new String(buffer, 0, buffer_size)));
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
                            buffer[buffer_size++] = (byte)c;
                        }
                        break;
                    case READ_TYPE:
                        if (Character.isWhitespace(c)) {
                            String identifier = new String(buffer, 0, buffer_size);
                            state = LexerState.SEEK_ANY;
                            buffer_size = 0;

                            if (identifier.startsWith("__")) {
                                System.out.println(identifier);
                                builtins.get(identifier).execute(stack);
                            } else {
                                System.out.println("this is not real");
                            }
                        } else if (c == '$') {
                            String typename = new String(buffer, 0, buffer_size);
                            // FIXME: this is excessively clobbered together
                            type = SillyType.Type.valueOf(typename.toUpperCase());
                            buffer_size = 0;
                            state = LexerState.READ_NUM_LITERAL;
                        } else {
                            buffer[buffer_size++] = (byte)c;
                        }
                        break;
                    case READ_NUM_LITERAL:
                        if (Character.isWhitespace(c)) {
                            state = LexerState.SEEK_ANY;
                            String num = new String(buffer, 0, buffer_size);
                            buffer_size = 0;
                            switch (type) {
                                case I8:
                                    stack.push(new TypeI8(Byte.parseByte(num)));
                                    break;
                                case I16:
                                    stack.push(new TypeI16(Short.parseShort(num)));
                                    break;
                                case I32:
                                    stack.push(new TypeI32(Integer.parseInt(num)));
                                    break;
                                case I64:
                                    stack.push(new TypeI64(Long.parseLong(num)));
                                    break;
                                case U1:
                                    stack.push(new TypeU1(Byte.parseByte(num) != 0));
                                case F32:
                                    stack.push(new TypeF32(Float.parseFloat(num)));
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
