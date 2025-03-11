package edu.uob.CommandParser;

public abstract class CommandParser {
    protected String commandType;

    // Abstract method, subclasses must implement the logic of parsing specific commands
    public abstract boolean parseCommand(String command);

    // Factory method, creates and returns the corresponding command parser instance according to the command type
    public static CommandParser createParser(String command) {
        // Convert the command string to uppercase and remove leading
        // and trailing spaces to facilitate subsequent analysis
        String liftCommand = command.trim().toUpperCase();

        if (liftCommand.startsWith("USE ")) {
            return new UseCommandParser();
        } else if (liftCommand.startsWith("CREATE DATABASE ")) {
            return new CreateCommandParser();
        } else if (liftCommand.startsWith("CREATE TABLE ")) {
            return new CreateCommandParser();
        } else if (liftCommand.startsWith("DROP DATABASE ")) {
            return new DropCommandParser();
        } else if (liftCommand.startsWith("DROP TABLE ")) {
            return new DropCommandParser();
        } else if (liftCommand.startsWith("ALTER TABLE ")) {
            return new AlterCommandParser();
        } else if (liftCommand.startsWith("INSERT INTO ")) {
            return new InsertCommandParser();
        } else if (liftCommand.startsWith("SELECT ")) {
            return new SelectCommandParser();
        } else if (liftCommand.startsWith("UPDATE ")) {
            return new UpdateCommandParser();
        } else if (liftCommand.startsWith("DELETE FROM ")) {
            return new DeleteCommandParser();
        } else if (liftCommand.startsWith("JOIN ")) {
            return new JoinCommandParser();
        }
        return null;
    }

    public String getCommandType(){
        return commandType;
    }
}