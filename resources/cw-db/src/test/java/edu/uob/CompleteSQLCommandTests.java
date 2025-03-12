package edu.uob;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;

public class CompleteSQLCommandTests {

    private DBServer server;

    @BeforeEach
    public void setup() {
        server = new DBServer();
    }

    private String generateRandomName() {
        String randomName = "";
        for (int i = 0; i < 10; i++) randomName += (char) (97 + (Math.random() * 25.0));
        return randomName;
    }

    private String sendCommandToServer(String command) {
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
                    return server.handleCommand(command);
                },
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    //==================================
    // USE test
    //==================================

    @Test
    public void testUseDatabase() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        String response = sendCommandToServer("USE " + randomName + ";");
        assertTrue(response.contains("[OK]"), "USE DATABASE should return [OK]");
    }

    @Test
    public void testUseNonExistentDatabase() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("USE " + randomName + ";");
        assertTrue(response.contains("[ERROR]"), "USE on non-existent database should return [ERROR]");
    }

    //==================================
    // CREATE test
    //==================================

    @Test
    public void testCreateDatabase() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATE DATABASE " + randomName + ";");
        assertTrue(response.contains("[OK]"), "CREATE DATABASE should return [OK]");
    }

    @Test
    public void testCreateDuplicateDatabase() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        String response = sendCommandToServer("CREATE DATABASE " + randomName + ";");
        assertTrue(response.contains("[ERROR]"), "Creating duplicate database should return [ERROR]");
    }

    @Test
    public void testCreateTableSimple() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE students;");
        assertTrue(response.contains("[OK]"), "CREATE TABLE without attributes should return [OK]");
    }

    @Test
    public void testCreateTableWithAttributes() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE students(name, age, grade);");
        assertTrue(response.contains("[OK]"), "CREATE TABLE with attributes should return [OK]");
    }

    @Test
    public void testCreateTableWithoutDatabase() {
        String response = sendCommandToServer("CREATE TABLE students;");
        assertTrue(response.contains("[ERROR]"), "CREATE TABLE without using a database should return [ERROR]");
    }

    @Test
    public void testCreateDuplicateTable() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students;");
        String response = sendCommandToServer("CREATE TABLE students;");
        assertTrue(response.contains("[ERROR]"), "Creating duplicate table should return [ERROR]");
    }

    //==================================
    // DROP
    //==================================

    @Test
    public void testDropDatabase() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        String response = sendCommandToServer("DROP DATABASE " + randomName + ";");
        assertTrue(response.contains("[OK]"), "DROP DATABASE should return [OK]");
        response = sendCommandToServer("USE " + randomName + ";");
        assertTrue(response.contains("[ERROR]"), "Dropped database should no longer exist");
    }

    @Test
    public void testDropNonExistentDatabase() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("DROP DATABASE " + randomName + ";");
        assertTrue(response.contains("[ERROR]"), "DROP on non-existent database should return [ERROR]");
    }

    @Test
    public void testDropTable() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students;");
        String response = sendCommandToServer("DROP TABLE students;");
        assertTrue(response.contains("[OK]"), "DROP TABLE should return [OK]");
        response = sendCommandToServer("SELECT * FROM students;");
        assertTrue(response.contains("[ERROR]"), "Dropped table should no longer exist");
    }

    @Test
    public void testDropNonExistentTable() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("DROP TABLE nonexistent;");
        assertTrue(response.contains("[ERROR]"), "DROP on non-existent table should return [ERROR]");
    }

    //==================================
    // ALTER
    //==================================

    @Test
    public void testAlterTableAdd() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name);");
        String response = sendCommandToServer("ALTER TABLE students ADD age;");
        assertTrue(response.contains("[OK]"), "ALTER TABLE ADD should return [OK]");
        response = sendCommandToServer("SELECT age FROM students;");
        assertTrue(response.contains("[OK]"), "New column should be accessible");
    }

    @Test
    public void testAlterTableDrop() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name, age);");
        String response = sendCommandToServer("ALTER TABLE students DROP age;");
        assertTrue(response.contains("[OK]"), "ALTER TABLE DROP should return [OK]");
        response = sendCommandToServer("SELECT age FROM students;");
        assertTrue(response.contains("[ERROR]"), "Dropped column should no longer exist");
    }

    @Test
    public void testAlterNonExistentTable() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("ALTER TABLE nonexistent ADD age;");
        assertTrue(response.contains("[ERROR]"), "ALTER on non-existent table should return [ERROR]");
    }

    @Test
    public void testAlterTableDropNonExistentAttribute() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name);");
        String response = sendCommandToServer("ALTER TABLE students DROP age;");
        assertTrue(response.contains("[ERROR]"), "DROP on non-existent attribute should return [ERROR]");
    }

    @Test
    public void testAlterTableAddDuplicateAttribute() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name, age);");
        String response = sendCommandToServer("ALTER TABLE students ADD age;");
        assertTrue(response.contains("[ERROR]"), "ADD duplicate attribute should return [ERROR]");
    }

    //==================================
    // INSERT
    //==================================

    @Test
    public void testInsertBasic() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name, age);");
        String response = sendCommandToServer("INSERT INTO students VALUES('John', 20);");
        assertTrue(response.contains("[OK]"), "INSERT should return [OK]");
        response = sendCommandToServer("SELECT * FROM students;");
        assertTrue(response.contains("John") && response.contains("20"), "Inserted values should be retrievable");
    }

    @Test
    public void testInsertIntoNonExistentTable() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("INSERT INTO nonexistent VALUES('John');");
        assertTrue(response.contains("[ERROR]"), "INSERT into non-existent table should return [ERROR]");
    }

    @Test
    public void testInsertDifferentDataTypes() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE data(string, integer, float, boolean, null_value);");
        String response = sendCommandToServer("INSERT INTO data VALUES('text', 10, 20.5, TRUE, NULL);");
         assertTrue(response.contains("[OK]"), "INSERT with different data types should return [OK]");
         response = sendCommandToServer("SELECT * FROM data;");
         assertTrue(response.contains("text") && response.contains("10") &&
                 response.contains("20.5") && response.contains("TRUE") &&
                 response.contains("NULL"), "All data types should be retrievable");
    }

    @Test
    public void testInsertNegativeNumbers() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE numbers(integer, float);");
        String response = sendCommandToServer("INSERT INTO numbers VALUES(-10, -20.5);");
        assertTrue(response.contains("[OK]"), "INSERT with negative numbers should return [OK]");
        response = sendCommandToServer("SELECT * FROM numbers;");
        assertTrue(response.contains("-10") && response.contains("-20.5"), "Negative numbers should be retrievable");
    }

    @Test
    public void testInsertPositiveNumbers() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE numbers(integer, float);");
        String response = sendCommandToServer("INSERT INTO numbers VALUES(+10, +20.5);");
        assertTrue(response.contains("[OK]"), "INSERT with positive numbers should return [OK]");
        response = sendCommandToServer("SELECT * FROM numbers;");
        assertTrue(response.contains("10") && response.contains("20.5"), "Positive numbers should be retrievable");
    }

    //==================================
    // SELECT
    //==================================

    @Test
    public void testSelectAll() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name, age);");
        sendCommandToServer("INSERT INTO students VALUES('John', 20);");
        sendCommandToServer("INSERT INTO students VALUES('Mary', 22);");
        String response = sendCommandToServer("SELECT * FROM students;");
        assertTrue(response.contains("[OK]"), "SELECT * should return [OK]");
        assertTrue(response.contains("John") && response.contains("20") &&
                        response.contains("Mary") && response.contains("22"),
                "All values should be retrievable with SELECT *");
    }

    @Test
    public void testSelectSpecificColumns() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name, age, grade);");
        sendCommandToServer("INSERT INTO students VALUES('John', 20, 'A');");
        String response = sendCommandToServer("SELECT name, grade FROM students;");
        assertTrue(response.contains("[OK]"), "SELECT specific columns should return [OK]");
        assertTrue(response.contains("John") && response.contains("A") &&
                !response.contains("20"), "Only specified columns should be retrieved");
    }

    @Test
    public void testSelectWithCondition() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name, age);");
        sendCommandToServer("INSERT INTO students VALUES('John', 20);");
        sendCommandToServer("INSERT INTO students VALUES('Mary', 22);");
        String response = sendCommandToServer("SELECT * FROM students WHERE name == 'John';");
        assertTrue(response.contains("[OK]"), "SELECT with condition should return [OK]");
        assertTrue(response.contains("John") && !response.contains("Mary"),
                "Only matching rows should be retrieved");
    }

    @Test
    public void testSelectFromNonExistentTable() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("SELECT * FROM nonexistent;");
        assertTrue(response.contains("[ERROR]"), "SELECT from non-existent table should return [ERROR]");
    }

    @Test
    public void testSelectNonExistentColumn() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name);");
        String response = sendCommandToServer("SELECT age FROM students;");
        assertTrue(response.contains("[ERROR]"), "SELECT non-existent column should return [ERROR]");
    }

    @Test
    public void testSelectWithComparisonConditions() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name, age);");
        sendCommandToServer("INSERT INTO students VALUES('John', 20);");
        sendCommandToServer("INSERT INTO students VALUES('Mary', 22);");
        sendCommandToServer("INSERT INTO students VALUES('Bob', 25);");

        // Equal
        String response = sendCommandToServer("SELECT * FROM students WHERE age == 22;");
        assertTrue(response.contains("Mary") && !response.contains("John") && !response.contains("Bob"),
                "Equal comparison should work correctly");

        // Greater than
        response = sendCommandToServer("SELECT * FROM students WHERE age > 20;");
        assertTrue(!response.contains("John") && response.contains("Mary") && response.contains("Bob"),
                "Greater than comparison should work correctly");

        // Less than
        response = sendCommandToServer("SELECT * FROM students WHERE age < 25;");
        assertTrue(response.contains("John") && response.contains("Mary") && !response.contains("Bob"),
                "Less than comparison should work correctly");

        // Greater than or equal
        response = sendCommandToServer("SELECT * FROM students WHERE age >= 22;");
        assertTrue(!response.contains("John") && response.contains("Mary") && response.contains("Bob"),
                "Greater than or equal comparison should work correctly");

        // Less than or equal
        response = sendCommandToServer("SELECT * FROM students WHERE age <= 22;");
        assertTrue(response.contains("John") && response.contains("Mary") && !response.contains("Bob"),
                "Less than or equal comparison should work correctly");

        // Not equal
        response = sendCommandToServer("SELECT * FROM students WHERE age != 22;");
        assertTrue(response.contains("John") && !response.contains("Mary") && response.contains("Bob"),
                "Not equal comparison should work correctly");
    }

    @Test
    public void testSelectWithLikeCondition() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name);");
        sendCommandToServer("INSERT INTO students VALUES('John');");
        sendCommandToServer("INSERT INTO students VALUES('Mary');");
        sendCommandToServer("INSERT INTO students VALUES('Jonathan');");

        String response = sendCommandToServer("SELECT * FROM students WHERE name LIKE 'Jo';");
        assertTrue(response.contains("John") && !response.contains("Mary") && response.contains("Jonathan"),
                "LIKE operator should match substrings");
    }

    @Test
    public void testSelectWithComplexConditions() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name, age, grade);");
        sendCommandToServer("INSERT INTO students VALUES('John', 20, 'A');");
        sendCommandToServer("INSERT INTO students VALUES('Mary', 22, 'B');");
        sendCommandToServer("INSERT INTO students VALUES('Bob', 20, 'C');");

        // AND condition
        String response = sendCommandToServer("SELECT * FROM students WHERE (age == 20) AND (grade == 'A');");
         assertTrue(response.contains("John") && !response.contains("Mary") && !response.contains("Bob"),
                 "AND condition should work correctly");

        // OR condition
         response = sendCommandToServer("SELECT * FROM students WHERE (name == 'John') OR (grade == 'B');");
         assertTrue(response.contains("John") && response.contains("Mary") && !response.contains("Bob"),
                 "OR condition should work correctly");

        // Nested condition
         response = sendCommandToServer("SELECT * FROM students WHERE ((age == 20) AND (grade == 'A')) OR (name == 'Mary');");
         assertTrue(response.contains("John") && response.contains("Mary") && !response.contains("Bob"),
                 "Nested conditions should work correctly");
    }

    //==================================
    // UPDATE
    //==================================

    @Test
    public void testUpdateBasic() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name, age);");
        sendCommandToServer("INSERT INTO students VALUES('John', 20);");
        String response = sendCommandToServer("UPDATE students SET age = 21 WHERE name == 'John';");
        assertTrue(response.contains("[OK]"), "UPDATE should return [OK]");
        response = sendCommandToServer("SELECT * FROM students;");
        assertTrue(response.contains("21") && !response.contains("20"), "Updated value should be retrieved");
    }

    @Test
    public void testUpdateMultipleColumns() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name, age, grade);");
        sendCommandToServer("INSERT INTO students VALUES('John', 20, 'B');");
        String response = sendCommandToServer("UPDATE students SET age = 21, grade = 'A' WHERE name == 'John';");
        assertTrue(response.contains("[OK]"), "UPDATE multiple columns should return [OK]");
        response = sendCommandToServer("SELECT * FROM students;");
        assertTrue(response.contains("21") && response.contains("A") &&
                        !response.contains("20") && !response.contains("B"),
                "All updated values should be retrieved");
    }

    @Test
    public void testUpdateWithComplexCondition() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name, age, grade);");
        sendCommandToServer("INSERT INTO students VALUES('John', 20, 'B');");
        sendCommandToServer("INSERT INTO students VALUES('Mary', 22, 'A');");
        String response = sendCommandToServer("UPDATE students SET grade = 'A+' WHERE (age > 18) AND (grade == 'A');");
         assertTrue(response.contains("[OK]"), "UPDATE with complex condition should return [OK]");
        response = sendCommandToServer("SELECT * FROM students;");
         assertTrue(response.contains("John") && response.contains("20") && response.contains("B") &&
                         response.contains("Mary") && response.contains("22") && response.contains("A+"),
                 "Only matching rows should be updated");
    }

    @Test
    public void testUpdateNonExistentTable() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("UPDATE nonexistent SET name = 'John';");
        assertTrue(response.contains("[ERROR]"), "UPDATE on non-existent table should return [ERROR]");
    }

    @Test
    public void testUpdateNonExistentColumn() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name);");
        String response = sendCommandToServer("UPDATE students SET age = 20;");
        assertTrue(response.contains("[ERROR]"), "UPDATE on non-existent column should return [ERROR]");
    }

    //==================================
    // DELETE
    //==================================

    @Test
    public void testDeleteBasic() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name);");
        sendCommandToServer("INSERT INTO students VALUES('John');");
        sendCommandToServer("INSERT INTO students VALUES('Mary');");
        String response = sendCommandToServer("DELETE FROM students WHERE name == 'John';");
        assertTrue(response.contains("[OK]"), "DELETE should return [OK]");
        response = sendCommandToServer("SELECT * FROM students;");
        assertTrue(!response.contains("John") && response.contains("Mary"),
                "Deleted rows should not be retrievable");
    }

    @Test
    public void testDeleteWithComplexCondition() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name, age, grade);");
        sendCommandToServer("INSERT INTO students VALUES('John', 20, 'B');");
        sendCommandToServer("INSERT INTO students VALUES('Mary', 22, 'A');");
        sendCommandToServer("INSERT INTO students VALUES('Bob', 21, 'A');");
        String response = sendCommandToServer("DELETE FROM students WHERE (age > 20) AND (grade == 'A');");
         assertTrue(response.contains("[OK]"), "DELETE with complex condition should return [OK]");
        response = sendCommandToServer("SELECT * FROM students;");
          assertTrue(response.contains("John") && !response.contains("Mary") && response.contains("Bob"),
                 "Only matching rows should be deleted");
    }

    @Test
    public void testDeleteFromNonExistentTable() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("DELETE FROM nonexistent WHERE name == 'John';");
        assertTrue(response.contains("[ERROR]"), "DELETE from non-existent table should return [ERROR]");
    }

    @Test
    public void testDeleteWithNonExistentAttribute() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name);");
        String response = sendCommandToServer("DELETE FROM students WHERE age == 20;");
        assertTrue(response.contains("[ERROR]"), "DELETE with non-existent attribute should return [ERROR]");
    }

    //==================================
    // JOIN
    //==================================

    @Test
    public void testJoinBasic() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name, studentId);");
        sendCommandToServer("CREATE TABLE grades(studentId, grade);");
        sendCommandToServer("INSERT INTO students VALUES('John', 1);");
        sendCommandToServer("INSERT INTO grades VALUES(1, 'A');");
        String response = sendCommandToServer("JOIN students AND grades ON studentId AND studentId;");
        assertTrue(response.contains("[OK]"), "JOIN should return [OK]");
        assertTrue(response.contains("John") && response.contains("A"),
                "JOIN should combine records from both tables");
    }

    @Test
    public void testJoinWithMultipleMatches() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name, courseId);");
        sendCommandToServer("CREATE TABLE courses(id, title);");
        sendCommandToServer("INSERT INTO students VALUES('John', 1);");
        sendCommandToServer("INSERT INTO students VALUES('Mary', 2);");
        sendCommandToServer("INSERT INTO students VALUES('Bob', 1);");
        sendCommandToServer("INSERT INTO courses VALUES(1, 'Math');");
        sendCommandToServer("INSERT INTO courses VALUES(2, 'English');");
        String response = sendCommandToServer("JOIN students AND courses ON courseId AND id;");
        assertTrue(response.contains("[OK]"), "JOIN with multiple matches should return [OK]");
        assertTrue(response.contains("John") && response.contains("Math") &&
                 response.contains("Mary") && response.contains("English") &&
                 response.contains("Bob"), "JOIN should combine all matching records");
    }

    @Test
    public void testJoinNonExistentTable() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name);");
        String response = sendCommandToServer("JOIN students AND nonexistent ON name AND name;");
        assertTrue(response.contains("[ERROR]"), "JOIN with non-existent table should return [ERROR]");
    }

    @Test
    public void testJoinNonExistentAttribute() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name);");
        sendCommandToServer("CREATE TABLE courses(title);");
        String response = sendCommandToServer("JOIN students AND courses ON age AND id;");
        assertTrue(response.contains("[ERROR]"), "JOIN with non-existent attribute should return [ERROR]");
    }

    //==================================
    // COMBINE
    //==================================

    @Test
    public void testCompleteWorkflow() {
        String randomName = generateRandomName();

        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");

        sendCommandToServer("CREATE TABLE students(name, age, grade);");
        sendCommandToServer("CREATE TABLE courses(courseId, title);");

        sendCommandToServer("INSERT INTO students VALUES('John', 20, 'B');");
        sendCommandToServer("INSERT INTO students VALUES('Mary', 22, 'A');");
        sendCommandToServer("INSERT INTO courses VALUES(101, 'Math');");
        sendCommandToServer("INSERT INTO courses VALUES(102, 'English');");

        sendCommandToServer("ALTER TABLE students ADD courseId;");

        sendCommandToServer("UPDATE students SET courseId = 101 WHERE name == 'John';");
        sendCommandToServer("UPDATE students SET courseId = 102 WHERE name == 'Mary';");

        String response = sendCommandToServer("SELECT * FROM students;");
        assertTrue(response.contains("John") && response.contains("Mary") &&
                        response.contains("101") && response.contains("102"),
                "All operations should work in sequence");

        response = sendCommandToServer("JOIN students AND courses ON courseId AND courseId;");
        assertTrue(response.contains("John") && response.contains("Math") &&
                        response.contains("Mary") && response.contains("English"),
                "JOIN should work with previously modified tables");

        sendCommandToServer("DELETE FROM students WHERE grade == 'B';");
        response = sendCommandToServer("SELECT * FROM students;");
        assertTrue(!response.contains("John") && response.contains("Mary"),
                "DELETE should work on previously modified tables");

        sendCommandToServer("DROP TABLE courses;");
        response = sendCommandToServer("SELECT * FROM courses;");
        assertTrue(response.contains("[ERROR]"), "DROP TABLE should work in sequence");
    }

    //==================================
    // WRONG SYNTAX
    //==================================

    @Test
    public void testMissingSemicolon() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATE DATABASE " + randomName);
        assertTrue(response.contains("[ERROR]"), "Command without semicolon should return [ERROR]");
    }

    @Test
    public void testInvalidCommand() {
        String response = sendCommandToServer("INVALID COMMAND;");
        assertTrue(response.contains("[ERROR]"), "Invalid command should return [ERROR]");
    }

    @Test
    public void testWhitespace() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATE    DATABASE    " + randomName + ";");
        assertTrue(response.contains("[OK]"), "Commands with extra whitespace should be parsed correctly");

        response = sendCommandToServer("USE   " + randomName + ";");
        assertTrue(response.contains("[OK]"), "Commands with extra whitespace should be parsed correctly");

        response = sendCommandToServer("CREATE    TABLE    students   (   name ,  age  );");
        assertTrue(response.contains("[OK]"), "Commands with extra whitespace should be parsed correctly");
    }

    @Test
    public void testInvalidSyntax() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");

        // Missing value in INSERT
        String response = sendCommandToServer("INSERT INTO students VALUES();");
        assertTrue(response.contains("[ERROR]"), "INSERT with missing values should return [ERROR]");

        // Invalid condition in WHERE clause
        response = sendCommandToServer("SELECT * FROM students WHERE;");
        assertTrue(response.contains("[ERROR]"), "SELECT with invalid WHERE clause should return [ERROR]");

        // Invalid syntax in CREATE TABLE
        response = sendCommandToServer("CREATE TABLE students(;");
        assertTrue(response.contains("[ERROR]"), "CREATE TABLE with invalid syntax should return [ERROR]");
    }

    @Test
    public void testCaseInsensitivity() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("create database " + randomName + ";");
        assertTrue(response.contains("[OK]"), "Commands should be case insensitive");

        response = sendCommandToServer("USE " + randomName + ";");
        assertTrue(response.contains("[OK]"), "Database name should be case sensitive");

        response = sendCommandToServer("CREATE TABLE students(name);");
        assertTrue(response.contains("[OK]"), "CREATE TABLE should work with lowercase");

        response = sendCommandToServer("insert into students values('John');");
        assertTrue(response.contains("[OK]"), "INSERT should work with lowercase");

        response = sendCommandToServer("SELECT * from students;");
        assertTrue(response.contains("[OK]") && response.contains("John"), "SELECT should work with mixed case");
    }

    @Test
    public void testDataTypeConversions() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE data(value);");

        sendCommandToServer("INSERT INTO data VALUES(10);");
        String response = sendCommandToServer("SELECT * FROM data WHERE value == '10';");
        assertTrue(response.contains("[OK]") && response.contains("10"),
                "Numeric values should be comparable with string representations");

        sendCommandToServer("INSERT INTO data VALUES(TRUE);");
        response = sendCommandToServer("SELECT * FROM data WHERE value == TRUE;");
        assertTrue(response.contains("[OK]") && response.contains("TRUE"),
                "Boolean values should be retrievable with exact match");
    }

    @Test
    public void testEmptyTableOperations() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE empty();");

        // Insert with no attributes
        String response = sendCommandToServer("INSERT INTO empty VALUES();");
        assertTrue(response.contains("[OK]"), "INSERT into table with no attributes should return [OK]");

        // Select from empty table
        response = sendCommandToServer("SELECT * FROM empty;");
        assertTrue(response.contains("[OK]"), "SELECT from table with no attributes should return [OK]");

        // Update empty table
        response = sendCommandToServer("UPDATE empty SET id = 1 WHERE id == 1;");
        assertTrue(response.contains("[ERROR]"), "UPDATE on non-existent attribute should return [ERROR]");
    }

    @Test
    public void testSpecialCharactersInValues() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE special(text);");

        String response = sendCommandToServer("INSERT INTO special VALUES('Hello, World!');");
        assertTrue(response.contains("[OK]"), "INSERT with special characters should return [OK]");

        response = sendCommandToServer("INSERT INTO special VALUES('SELECT * FROM table;');");
        assertTrue(response.contains("[OK]"), "INSERT with SQL-like text should return [OK]");

        response = sendCommandToServer("SELECT * FROM special;");
        assertTrue(response.contains("Hello, World!") && response.contains("SELECT * FROM table;"),
                "Special characters should be retrievable");
    }

    @Test
    public void testSelectWithMissingCondition() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name);");

        String response = sendCommandToServer("SELECT * FROM students WHERE;");
        assertTrue(response.contains("[ERROR]"), "SELECT with missing condition should return [ERROR]");

        response = sendCommandToServer("SELECT * FROM students WHERE name ==;");
        assertTrue(response.contains("[ERROR]"), "SELECT with incomplete condition should return [ERROR]");
    }

    @Test
    public void testNullComparisons() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE data(value);");
        sendCommandToServer("INSERT INTO data VALUES(NULL);");
        sendCommandToServer("INSERT INTO data VALUES('text');");

        String response = sendCommandToServer("SELECT * FROM data WHERE value == NULL;");
        assertTrue(response.contains("[OK]"), "NULL comparisons should work");
        assertTrue(response.contains("NULL") && !response.contains("text"),
                "NULL comparisons should only return NULL values");
    }

    @Test
    public void testUpdateWithoutWhere() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name, age);");
        sendCommandToServer("INSERT INTO students VALUES('John', 20);");
        sendCommandToServer("INSERT INTO students VALUES('Mary', 22);");

        String response = sendCommandToServer("UPDATE students SET age = 25;");
        assertTrue(response.contains("[ERROR]"), "UPDATE without WHERE clause should return [ERROR]");
    }

    @Test
    public void testDeleteWithoutWhere() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name);");
        sendCommandToServer("INSERT INTO students VALUES('John');");

        String response = sendCommandToServer("DELETE FROM students;");
        assertTrue(response.contains("[ERROR]"), "DELETE without WHERE clause should return [ERROR]");
    }

    @Test
    public void testAttributeNameCaseSensitivity() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(Name, Age);");

        // Try to select with different case
        String response = sendCommandToServer("SELECT name, age FROM students;");
        assertTrue(response.contains("[ERROR]"), "Attribute names should be case sensitive");

        // Try to insert with correct case
        response = sendCommandToServer("INSERT INTO students VALUES('John', 20);");
        assertTrue(response.contains("[OK]"), "INSERT with correct attribute case should work");

        // Try to select with correct case
        response = sendCommandToServer("SELECT Name, Age FROM students;");
        assertTrue(response.contains("[OK]") && response.contains("John") && response.contains("20"),
                "SELECT with correct attribute case should work");
    }

    @Test
    public void testMalformedCommands() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");

        // Malformed CREATE
        String response = sendCommandToServer("CREATE students;");
        assertTrue(response.contains("[ERROR]"), "Malformed CREATE should return [ERROR]");

        // Malformed DROP
        response = sendCommandToServer("DROP students;");
        assertTrue(response.contains("[ERROR]"), "Malformed DROP should return [ERROR]");

        // Malformed ALTER
        response = sendCommandToServer("ALTER students ADD name;");
        assertTrue(response.contains("[ERROR]"), "Malformed ALTER should return [ERROR]");
    }

    @Test
    public void testEmptyValues() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students(name);");

        String response = sendCommandToServer("INSERT INTO students VALUES('');");
        assertTrue(response.contains("[OK]"), "INSERT with empty string should return [OK]");

        response = sendCommandToServer("SELECT * FROM students WHERE name == '';");
        assertTrue(response.contains("[OK]") && !response.contains("John"),
                "Empty string comparisons should work");
    }
}
