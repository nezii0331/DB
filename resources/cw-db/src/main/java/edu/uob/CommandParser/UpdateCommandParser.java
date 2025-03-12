package edu.uob.CommandParser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateCommandParser extends CommandParser {
    private List<String> nameValuePairs;
    private String tableName;
    private String condition;

    public UpdateCommandParser() {
        this.nameValuePairs = new ArrayList<>();
    }

    @Override
    public boolean parseCommand(String command) {
        return parseUpdateCommand(command);
    }

    private void parseNameValueList(String nameValueStr) {
        boolean inQuotes = false;
        StringBuilder currentPair = new StringBuilder();

        for (int i = 0; i < nameValueStr.length(); i++) {
            char c = nameValueStr.charAt(i);

            if (c == '\'') {
                inQuotes = !inQuotes;
                currentPair.append(c);
            } else if (c == ',' && !inQuotes) {
                nameValuePairs.add(currentPair.toString().trim());
                currentPair = new StringBuilder();
            } else {
                currentPair.append(c);
            }
        }
        if (currentPair.length() > 0) {
            nameValuePairs.add(currentPair.toString().trim());
        }
    }

    private boolean parseUpdateCommand(String command) {
        commandType = "UPDATE";
        Pattern pattern = Pattern.compile("UPDATE\\s+([a-zA-Z0-9]+)\\s+SET\\s+(.+?)\\s+WHERE\\s+(.+)");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            tableName = matcher.group(1).toLowerCase();
            String nameValueStr = matcher.group(2).trim();
            condition = matcher.group(3).trim();
            parseNameValueList(nameValueStr);
            return true;
        }
        return false;
    }

    public List<String> getNameValuePare() {
        return nameValuePairs;
    }

    public String getTableName() {
        return tableName;
    }

    public String getCondition() {
        return condition;
    }
}