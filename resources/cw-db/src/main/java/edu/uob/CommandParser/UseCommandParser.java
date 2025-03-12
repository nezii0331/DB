package edu.uob.CommandParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UseCommandParser extends CommandParser {
    private String databaseName;

    @Override
    public boolean parseCommand(String command) {
        return parseUseCommand(command);
    }

    public boolean parseUseCommand(String command) {
        commandType = "USE";
        Pattern pattern = Pattern.compile("\\s*USE\\s+([a-zA-Z0-9_]+)\\s*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            databaseName = matcher.group(1);
            return true;
        }
        return false;
    }

    public String getDatabaseName() {
        return databaseName;
    }
}