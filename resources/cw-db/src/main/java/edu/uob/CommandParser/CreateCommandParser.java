package edu.uob.CommandParser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateCommandParser extends CommandParser {
    private String databaseName;
    private String tableName;
    private List<String> columnNames;

    public CreateCommandParser() {
        this.columnNames = new ArrayList<>();
    }

    @Override
    public boolean parseCommand(String command) {
        if (command.toUpperCase().startsWith("CREATE DATABASE")) {
            return parseCreateDatabaseCommand(command);
        } else if (command.toUpperCase().startsWith("CREATE TABLE")) {
            return parseCreateTableCommand(command);
        }
        return false;
    }

    private boolean parseCreateTableCommand(String command) {
        commandType = "CREATE TABLE";
        Pattern pattern = Pattern.compile("\\s*CREATE\\s+TABLE\\s+([a-zA-Z0-9]+)\\s*(?:\\(\\s*(.*?)\\s*\\))?\\s*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            tableName = matcher.group(1).toLowerCase();
            if (matcher.group(2) != null) {
                String columnsStr = matcher.group(2).trim();
                if (!columnsStr.isEmpty()) {
                    String[] columns = columnsStr.split("\\s*,\\s*");
                    for (String column : columns) {
                        columnNames.add(column.trim());
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean parseCreateDatabaseCommand(String command) {
        commandType = "CREATE DATABASE";
        Pattern pattern = Pattern.compile("CREATE\\s+DATABASE\\s+([a-zA-Z0-9]+)");
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

    public String[] getColumnNames() {
        return columnNames.toArray(new String[0]);
    }
}