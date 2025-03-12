package edu.uob.CommandParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelectCommandParser extends CommandParser {
    private String tableName;
    private List<String> columnNames;
    private String condition;


    public SelectCommandParser() {
        this.columnNames = new ArrayList<>();
    }

    @Override
    public boolean parseCommand(String command) {
        return parseSelectCommand(command);
    }

    private boolean parseSelectCommand(String command) {
        commandType = "SELECT";
        Pattern pattern;
        Matcher matcher;

        if (command.toUpperCase().contains(" WHERE ")) {
            pattern = Pattern.compile("SELECT\\s+(.+?)\\s+FROM\\s+([a-zA-Z0-9]+)\\s+WHERE\\s+(.+)", Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(command);
            if (matcher.find()) {
                String columnsStr = matcher.group(1).trim();
                tableName = matcher.group(2).toLowerCase();
                condition = matcher.group(3).trim();
                parseColumnList(columnsStr);
                return true;
            }
        } else {
            pattern = Pattern.compile("SELECT\\s+(.+?)\\s+FROM\\s+([a-zA-Z0-9]+)", Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(command);
            if (matcher.find()) {
                String columnsStr = matcher.group(1).trim();
                tableName = matcher.group(2).toLowerCase();
                condition = null;
                parseColumnList(columnsStr);
                return true;
            }
        }
        return false;
    }

    private void parseColumnList(String columnsStr) {
        columnNames.clear();
        if (columnsStr.equals("*")) {
            columnNames.add("*");
        } else {
            String[] columns = columnsStr.split("\\s*,\\s*");
            columnNames.addAll(Arrays.asList(columns));
        }
    }

    public String[] getColumnNames() {
        return columnNames.toArray(new String[0]);
    }

    public String getCondition() {
        return condition;
    }

    public String getTableName() {
        return tableName;
    }
}