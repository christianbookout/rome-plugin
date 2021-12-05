import org.junit.Test;
import romeplugin.sillylang.Interpreter;
import romeplugin.sillylang.Lexer;
import romeplugin.sillylang.SillyProgram;
import romeplugin.sillylang.Subroutine;
import romeplugin.sillylang.builtin.Builtin;
import romeplugin.sillylang.types.SillyType;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class SillyLangTests {
    Interpreter interpreter = new Interpreter();
    Lexer lexer = new Lexer();

    private InputStream sToIs(String s) {
        return new ByteArrayInputStream(s.getBytes());
    }
    private void runScript(String script) throws IOException {
        interpreter.interpret(new ByteArrayInputStream(script.getBytes()));
    }

    @Test
    public void testIntegerTypes() throws IOException {
        interpreter.interpret(sToIs("i8$127"));
        assertEquals((byte)127, interpreter.pop().getValue());
        interpreter.interpret(sToIs("i16$9999"));
        assertEquals((short)9999, interpreter.pop().getValue());
        interpreter.interpret(sToIs("i32$10"));
        assertEquals(10, interpreter.pop().getValue());
        interpreter.interpret(sToIs("i64$100000000000000"));
        assertEquals(100000000000000L, interpreter.pop().getValue());
    }

    @Test
    public void testFloatingPoint() throws IOException {
        interpreter.interpret(sToIs("f32$1000"));
        assertEquals(1000.0f, interpreter.pop().getValue());

        interpreter.interpret(sToIs("f64$0.00057"));
        assertEquals(0.00057D, interpreter.pop().getValue());
    }

    @Test
    public void testAddition() throws IOException {
        runScript("f64$10.2 f64$0.0 __add");
        assertEquals(10.2D, interpreter.pop().getValue());
        runScript("f64$10.2 f64$2.5 __add");
        assertEquals(12.7D, interpreter.pop().getValue());
    }

    @Test
    public void testEmptySwap() {
        assertThrows(NoSuchElementException.class, ()->runScript("__swap"));
    }



    @Test
    public void testBuiltinClass() throws IOException {
        interpreter.interpret(sToIs("\"java.lang.System\" __class __duplicate __print"));
        assertEquals(System.class, interpreter.pop().getValue());
        interpreter.interpret(sToIs("\"hi world\" i32$0 \"out\" \"java.lang.System\" __class __field __field_get \"java.lang.String\" __class i8$1 \"println\" \"java.io.PrintStream\" __class __method __execute"));
    }

    @Test
    public void testPrint() throws IOException {
        interpreter.interpret(sToIs("\"yo vivo\" __print"));
        interpreter.interpret(sToIs("\"yo vivo\" __duplicate __print __print"));
    }


    @Test
    public void testSwap() throws IOException {
        interpreter.clear();
        interpreter.interpret(sToIs("i32$1 i32$20 __swap"));
        assertEquals(1, interpreter.pop().getValue());
        assertEquals(20, interpreter.pop().getValue());
    }

    @Test
    public void testSubroutineParse() throws IOException {
        Subroutine routine = lexer.parseSubroutine(new ByteArrayInputStream("f64$10.5 f64$22.5 __add".getBytes()));
        ArrayDeque<SillyType> funny = new ArrayDeque<>();
        routine.execute(funny, new HashMap<>());
        assertEquals(33.D, funny.pop().getValue());
    }

    @Test
    public void testProg() throws IOException {
        SillyProgram program = lexer.parseProgram(new ByteArrayInputStream("run -> { i64$0 i64$921 __add }".getBytes()));
        Deque<SillyType> stack = new ArrayDeque<>();
        program.getRoutine("run").execute(stack, new HashMap<>());
        assertEquals(921L, stack.pop().getValue());
    }
}
