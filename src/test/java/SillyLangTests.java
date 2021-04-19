import org.junit.Test;
import romeplugin.sillylang.Interpreter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class SillyLangTests {
    Interpreter interpreter = new Interpreter();

    private InputStream stois(String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    @Test
    public void testI32() {
        interpreter.interpret(stois("i32$10 __print "));
    }

    @Test
    public void testBuiltinClass() {
        interpreter.interpret(stois("\"java.lang.System\" __class __print "));
        interpreter.interpret(stois("\"hi world\" i32$0 \"out\" \"java.lang.System\" __class __field __field_get \"java.lang.String\" __class i8$1 \"println\" \"java.io.PrintStream\" __class __method __execute "));
    }

    @Test
    public void testPrint() {
        System.out.println("test11");
        interpreter.interpret(stois("\"yo vivo en vivo\" __print "));
        interpreter.interpret(stois("\"yo vivo en vivo\" __duplicate __print __print "));
    }
}
