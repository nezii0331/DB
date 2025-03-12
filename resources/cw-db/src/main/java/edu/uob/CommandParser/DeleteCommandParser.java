package edu.uob.CommandParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeleteCommandParser extends CommandParser {
    private String tableName;
    private String condition;

    @Override
    public boolean parseCommand(String command) {
        return parseDeleteCommand(command);
    }

    private boolean parseDeleteCommand(String command) {
        commandType = "DELETE";
        Pattern pattern = Pattern.compile("DELETE\\s+FROM\\s+([a-zA-Z0-9]+)\\s+WHERE\\s+(.+)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            tableName = matcher.group(1).toLowerCase();
            condition = matcher.group(2).trim();
            return true;
        }
        return false;
    }

    public String getTableName() {
        return tableName;
    }

    public String getCondition() {
        return condition;
    }
}
