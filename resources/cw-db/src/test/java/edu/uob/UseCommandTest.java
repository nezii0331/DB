package test.java.edu.uob;
import org.junit.jupiter.api.Test;

public class UseCommandTest {
    @Test
    void testParseCommand() {
        UseCommandParser parser = new UseCommandParser();
        assertTrue(parser.parseCommand("USE markbook;"));
        assertEquals("markbook", parser.getDatabaseName());
    }

    @Test
    void testInvalidCommand() {
        UseCommandParser parser = new UseCommandParser();
        assertFalse(parser.parseCommand("USE;")); // This should return false
    }
}