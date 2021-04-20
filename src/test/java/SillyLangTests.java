import org.junit.Test;
import romeplugin.sillylang.Interpreter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class SillyLangTests {
    Interpreter interpreter = new Interpreter();

    private InputStream sToIs(String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    @Test
    public void testI32() {
        interpreter.interpret(sToIs("i32$10"));
        assertEquals(10, interpreter.pop().getValue());
    }

    @Test
    public void testBuiltinClass() {
        interpreter.interpret(sToIs("\"java.lang.System\" __class __duplicate __print"));
        assertEquals(System.class, interpreter.pop().getValue());
        interpreter.interpret(sToIs("\"hi world\" i32$0 \"out\" \"java.lang.System\" __class __field __field_get \"java.lang.String\" __class i8$1 \"println\" \"java.io.PrintStream\" __class __method __execute"));
    }

    @Test
    public void testPrint() {
        interpreter.interpret(sToIs("\"yo vivo\" __print"));
        interpreter.interpret(sToIs("\"yo vivo\" __duplicate __print __print"));
    }


    @Test
    public void testSwap() {
        interpreter.clear();
        interpreter.interpret(sToIs("i32$1 i32$20 __swap"));
        assertEquals(1, interpreter.pop().getValue());
        assertEquals(20, interpreter.pop().getValue());
    }
}
