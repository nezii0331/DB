package edu.uob.CommandParser;

public abstract class CommandParser {
    protected String commandType;

    public static CommandParser createParser(String command) {
        String upperCommand = command.trim().toUpperCase();

        if (upperCommand.startsWith("USE ")) {
            return new UseCommandParser();
        } else if (upperCommand.startsWith("CREATE DATABASE ")) {
            return new CreateCommandParser();
        } else if (upperCommand.startsWith("CREATE TABLE ")) {
            return new CreateCommandParser();
        } else if (upperCommand.startsWith("DROP DATABASE ")) {
            return new DropCommandParser();
        } else if (upperCommand.startsWith("DROP TABLE ")) {
            return new DropCommandParser();
        } else if (upperCommand.startsWith("ALTER TABLE ")) {
            return new AlterCommandParser();
        } else if (upperCommand.startsWith("INSERT INTO ")) {
            return new InsertCommandParser();
        } else if (upperCommand.startsWith("SELECT ")) {
            return new SelectCommandParser();
        } else if (upperCommand.startsWith("UPDATE ")) {
            return new UpdateCommandParser();
        } else if (upperCommand.startsWith("DELETE FROM ")) {
            return new DeleteCommandParser();
        } else if (upperCommand.startsWith("JOIN ")) {
            return new JoinCommandParser();
        }

        return null;
    }

    public abstract boolean parseCommand(String command);

    public String getCommandType() {
        return commandType;
    }
}