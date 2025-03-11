package test.java.edu.uob;
import org.junit.jupiter.api.Test;

public class UseCommandTest {
//    @Test
//    void testParseCommand() {
//        UseCommandParser parser = new UseCommandParser();
//        assertTrue(parser.parseCommand("USE markbook;"));
//        assertEquals("markbook", parser.getDatabaseName());
//    }
//
//    @Test
//    void testInvalidCommand() {
//        UseCommandParser parser = new UseCommandParser();
//        assertFalse(parser.parseCommand("USE;")); // This should return false
//    }

    @Test
    void testCreateDatabase() {
        CreateCommandParser parser = new CreateCommandParser();
        assertTrue(parser.parseCommand("CREATE DATABASE testdb;"));
        assertEquals("testdb", parser.getDatabaseName());
        assertEquals("CREATE DATABASE", parser.getCommandType());
    }

    @Test
    void testCreateDatabaseInvalidName() {
        CreateCommandParser parser = new CreateCommandParser();
        assertFalse(parser.parseCommand("CREATE DATABASE @invalid;"));
    }

    @Test
    void testCreateDatabaseMissingName() {
        CreateCommandParser parser = new CreateCommandParser();
        assertFalse(parser.parseCommand("CREATE DATABASE;"));
    }

    @Test
    void testCreateTable() {
        CreateCommandParser parser = new CreateCommandParser();
        assertTrue(parser.parseCommand("CREATE TABLE students;"));
        assertEquals("students", parser.getTableName());
        assertEquals("CREATE TABLE", parser.getCommandType());
    }

    @Test
    void testCreateTableWithColumns() {
        CreateCommandParser parser = new CreateCommandParser();
        assertTrue(parser.parseCommand("CREATE TABLE students (id, name, age);"));
        assertEquals("students", parser.getTableName());
        String[] expectedColumns = {"id", "name", "age"};
        assertArrayEquals(expectedColumns, parser.getColumnName());
    }

    @Test
    void testCreateTableWithSpaces() {
        CreateCommandParser parser = new CreateCommandParser();
        assertTrue(parser.parseCommand("CREATE   TABLE   employees  (  id  ,  name  ,  department  )  ;"));
        assertEquals("employees", parser.getTableName());
        String[] expectedColumns = {"id", "name", "department"};
        assertArrayEquals(expectedColumns, parser.getColumnName());
    }

    @Test
    void testCreateTableCaseInsensitive() {
        CreateCommandParser parser = new CreateCommandParser();
        assertTrue(parser.parseCommand("create table PRODUCTS (id, NAME, price);"));
        assertEquals("products", parser.getTableName());
        String[] expectedColumns = {"id", "NAME", "price"};
        assertArrayEquals(expectedColumns, parser.getColumnName());
    }

    @Test
    void testCreateTableInvalidCommand() {
        CreateCommandParser parser = new CreateCommandParser();
        assertFalse(parser.parseCommand("CREATE TABLE;"));
    }
}