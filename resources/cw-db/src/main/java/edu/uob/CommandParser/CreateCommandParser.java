package edu.uob.CommandParser;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateCommandParser extends CommandParser {
    private String databaseName;
    private String tableName;
    private List<String> columnNames;

    @Override
    public boolean parseCommand(String command) {
        if (command.toUpperCase().startsWith("CREATE DATABASE")) {
            return parseCreateDatabase(command);
        } else if (command.toUpperCase().startsWith("CREATE TABLE")) {
            return parseCreateTable(command);
        }
        return false;
    }

    private boolean parseCreateDatabase(String command) {
        commandType = "CREATE DATABASE";
        String[] parts = command.trim().split("\\s+");

        if (parts.length == 3 && parts[0].equalsIgnoreCase("CREATE") && parts[1].equalsIgnoreCase("DATABASE")) {
            String dbName = parts[2].toLowerCase();

            if (!dbName.matches("[a-zA-Z0-9_-]+")) {
                return false;
            }
            databaseName = dbName;
            return true;
        }
        return false;
    }

    private boolean parseCreateTable(String command) {
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


    public String[] getColumnName() {
        return columnNames.toArray(new String[0]);
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getTableName() {
        return tableName;
    }

    public CreateCommandParser() {
        this.columnNames = new ArrayList<>();
    }
}
