package edu.uob.CommandParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DropCommandParser extends CommandParser {
    private String databaseName;
    private String tableName;

    @Override
    public boolean parseCommand(String command) {
        if (command.toUpperCase().startsWith("DROP DATABASE")) {
            return parseDropDatabase(command);
        } else if (command.toUpperCase().startsWith("DROP TABLE")) {
            return parseDropTable(command);
        }
        return false;
    }

    private boolean parseDropTable(String command) {
        commandType = "DROP TABLE";
        Pattern pattern = Pattern.compile("DROP\\s+TABLE\\s+([a-zA-Z0-9]+)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            tableName = matcher.group(1).toLowerCase();
            return true;
        }
        return false;
    }

    private boolean parseDropDatabase(String command) {
        commandType = "DROP DATABASE";
        Pattern pattern = Pattern.compile("DROP\\s+DATABASE\\s+([a-zA-Z0-9]+)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            databaseName = matcher.group(1).toLowerCase();
            return true;
        }
        return false;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getTableName() {
        return tableName;
    }
}

