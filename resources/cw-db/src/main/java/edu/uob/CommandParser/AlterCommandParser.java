package edu.uob.CommandParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlterCommandParser extends CommandParser {
    private String alterType;
    private String attributeName;
    private String tableName;

    @Override
    public boolean parseCommand(String command) {
        return parseAlterTableCommand(command);
    }

    private boolean parseAlterTableCommand(String command) {
        commandType = "ALTER TABLE";
        Pattern pattern = Pattern.compile("ALTER\\s+TABLE\\s+([a-zA-Z0-9]+)\\s+(ADD|DROP)\\s+([a-zA-Z0-9]+)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            tableName = matcher.group(1).toLowerCase();
            alterType = matcher.group(2).toUpperCase();
            attributeName = matcher.group(3).toLowerCase();
            return true;
        }
        return false;
    }

    public String getAlterType() {
        return alterType;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getTableName() {
        return tableName;
    }

}