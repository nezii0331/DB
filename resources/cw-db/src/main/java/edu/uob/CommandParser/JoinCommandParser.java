package edu.uob.CommandParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JoinCommandParser extends CommandParser {
    private String secondTableName;
    private String secondJoinColumn;
    private String firstJoinColumn;
    private String tableName;

    @Override
    public boolean parseCommand(String command) {
        return parseJoinCommand(command);
    }

    private boolean parseJoinCommand(String command) {
        commandType = "JOIN";
        Pattern pattern = Pattern.compile("JOIN\\s+([a-zA-Z0-9]+)\\s+AND\\s+([a-zA-Z0-9]+)\\s+ON\\s+([a-zA-Z0-9]+)\\s+AND\\s+([a-zA-Z0-9]+)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            tableName = matcher.group(1).toLowerCase();
            secondTableName = matcher.group(2).toLowerCase();
            firstJoinColumn = matcher.group(3).toLowerCase();
            secondJoinColumn = matcher.group(4).toLowerCase();
            return true;
        }
        return false;
    }

    public String getFirstJoinColumn() {
        return firstJoinColumn;
    }

    public String getSecondJoinColumn() {
        return secondJoinColumn;
    }

    public String getSecondTableName() {
        return secondTableName;
    }

    public String getTableName() {
        return tableName;
    }
}