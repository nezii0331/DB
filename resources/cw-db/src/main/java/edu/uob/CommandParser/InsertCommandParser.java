package edu.uob.CommandParser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InsertCommandParser extends CommandParser {
    private String tableName;
    private List<String> values;

    public InsertCommandParser() {
        this.values = new ArrayList<>();
    }

    @Override
    public boolean parseCommand(String command) {
        return parseInsertCommand(command);
    }

    private boolean parseInsertCommand(String command) {
        commandType = "INSERT INTO";
        Pattern pattern = Pattern.compile("\\s*INSERT\\s+INTO\\s+([a-zA-Z0-9]+)\\s+VALUES\\s*\\(\\s*(.+?)\\s*\\)\\s*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            tableName = matcher.group(1).toLowerCase();
            String valuesStr = matcher.group(2).trim();
            parseValueList(valuesStr);
            return true;
        }
        return false;
    }

    private void parseValueList(String valuesStr) {
        values.clear();
        boolean inQuotes = false;
        StringBuilder currentValue = new StringBuilder();

        for (int i = 0; i < valuesStr.length(); i++) {
            char c = valuesStr.charAt(i);

            if (c == '\'') {
                inQuotes = !inQuotes;
                currentValue.append(c);
            } else if (c == ',' && !inQuotes) {
                values.add(currentValue.toString().trim());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }
        
        if (currentValue.length() > 0) {
            values.add(currentValue.toString().trim());
        }
    }

    public String[] getValues() {
        return values.toArray(new String[0]);
    }

    public String getTableName() {
        return tableName;
    }
}