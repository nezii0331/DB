package edu.uob.DataStructure;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for executing SQL queries
 */
public class QueryAgent {
    private final Database database;

    public QueryAgent (Database database) {
        this.database = database;
    }

    /**
     * Execute SQL query
     */
    public String execute(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "[ERROR] Empty query";
        }
        String trimmedQuery = query.trim();
        String upperQuery = trimmedQuery.toUpperCase();

        try {
            if (upperQuery.startsWith("CREATE TABLE ")) {
                return handleCreateTable(trimmedQuery);
            }
            else if (upperQuery.startsWith("INSERT INTO ")) {
                return handleInsert(trimmedQuery);
            }
            else if (upperQuery.startsWith("SELECT ")) {
                return handleSelect(trimmedQuery);
            }
            else if (upperQuery.startsWith("UPDATE ")) {
                return handleUpdate(trimmedQuery);
            }
            else if (upperQuery.startsWith("DELETE FROM ")) {
                return handleDelete(trimmedQuery);
            }
            else if (upperQuery.startsWith("ALTER TABLE ")) {
                return handleAlterTable(trimmedQuery);
            }
            else if (upperQuery.startsWith("DROP TABLE ")) {
                return handleDropTable(trimmedQuery);
            }
            else if (upperQuery.startsWith("JOIN ")) {
                return handleJoin(trimmedQuery);
            }
            else {
                return "[ERROR] Unsupported query: " + query;
            }
        } catch (Exception e) {
            return "[ERROR] Query execution failed: " + e.getMessage();
        }
    }

    /**
     * Handle CREATE TABLE command
     */
    private String handleCreateTable(String query) {
        String tableDefinition = query.substring("CREATE TABLE ".length());
        String tableName;
        String[] columns = new String[0];
        // Parse table name and column definitions
        if (tableDefinition.contains("(")) {
            tableName = tableDefinition.substring(0, tableDefinition.indexOf("(")).trim();
            String columnsStr = tableDefinition.substring(
                    tableDefinition.indexOf("(") + 1,
                    tableDefinition.lastIndexOf(")")
            ).trim();
            columns = columnsStr.split("\\s*,\\s*");
        } else {
            tableName = tableDefinition.trim();
        }
        return database.createTable(tableName, columns);
    }

    /**
     * Handle INSERT INTO command
     */
    private String handleInsert(String query) {
        String insertDefinition = query.substring("INSERT INTO ".length());
        String tableName = insertDefinition.substring(0, insertDefinition.indexOf("VALUES")).trim();
        String valuesStr = insertDefinition.substring(
                insertDefinition.indexOf("(") + 1,
                insertDefinition.lastIndexOf(")")
        ).trim();
        String[] values = parseValues(valuesStr);
        return database.insertRow(tableName, values);
    }

    /**
     * Handle SELECT command
     */
    private String handleSelect(String query) {
        String selectDefinition = query.substring("SELECT ".length());
        String columnsStr;
        String tableName;
        String condition = null;

        // Parse column names
        int fromIndex = selectDefinition.toUpperCase().indexOf(" FROM ");
        columnsStr = selectDefinition.substring(0, fromIndex).trim();

        // Parse table name and condition
        String tableAndCondition = selectDefinition.substring(fromIndex + " FROM ".length()).trim();
        if (tableAndCondition.toUpperCase().contains(" WHERE ")) {
            int whereIndex = tableAndCondition.toUpperCase().indexOf(" WHERE ");
            tableName = tableAndCondition.substring(0, whereIndex).trim();
            condition = tableAndCondition.substring(whereIndex + " WHERE ".length()).trim();
        } else {
            tableName = tableAndCondition.trim();
        }

        String[] columnNames = columnsStr.equals("*") ?
                new String[]{"*"} :
                columnsStr.split("\\s*,\\s*");

        return database.select(tableName, columnNames, condition);
    }

    /**
     * Handle UPDATE command
     */
    private String handleUpdate(String query) {
        String updateDefinition = query.substring("UPDATE ".length());
        String tableName;
        List<String> nameValuePairs;
        String condition;
        int setIndex = updateDefinition.toUpperCase().indexOf(" SET ");
        tableName = updateDefinition.substring(0, setIndex).trim();

        // Parse name-value pairs and condition
        String setAndCondition = updateDefinition.substring(setIndex + " SET ".length());
        int whereIndex = setAndCondition.toUpperCase().indexOf(" WHERE ");
        String nameValueStr = setAndCondition.substring(0, whereIndex).trim();
        condition = setAndCondition.substring(whereIndex + " WHERE ".length()).trim();
        nameValuePairs = parseNameValuePairs(nameValueStr);
        return database.update(tableName, nameValuePairs, condition);
    }

    /**
     * Handle DELETE command
     */
    private String handleDelete(String query) {
        String deleteDefinition = query.substring("DELETE FROM ".length());
        String tableName;
        String condition;
        // Parse table name and condition
        int whereIndex = deleteDefinition.toUpperCase().indexOf(" WHERE ");
        tableName = deleteDefinition.substring(0, whereIndex).trim();
        condition = deleteDefinition.substring(whereIndex + " WHERE ".length()).trim();
        return database.delete(tableName, condition);
    }

    /**
     * Handle ALTER TABLE command
     */
    private String handleAlterTable(String query) {
        String alterDefinition = query.substring("ALTER TABLE ".length());
        String tableName;
        String alterationType;
        String attributeName;
        // Parse table name, operation type, and attribute name
        String[] parts = alterDefinition.split("\\s+");
        if (parts.length < 3) {
            return "[ERROR] Invalid ALTER TABLE syntax";
        }
        tableName = parts[0];
        alterationType = parts[1]; // ADD or DROP
        attributeName = parts[2];
        return database.alterTable(tableName, alterationType, attributeName);
    }

    /**
     * Handle DROP TABLE command
     */
    private String handleDropTable(String query) {
        String tableName = query.substring("DROP TABLE ".length()).trim();
        return database.dropTable(tableName);
    }

    /**
     * Handle JOIN command
     */
    private String handleJoin(String query) {
        String joinDefinition = query.substring("JOIN ".length());
        String[] parts = joinDefinition.split("\\s+");
        if (parts.length != 7 || !parts[1].equalsIgnoreCase("AND") ||
                !parts[3].equalsIgnoreCase("ON") || !parts[5].equalsIgnoreCase("AND")) {
            return "[ERROR] Invalid JOIN syntax";
        }
        String tableName1 = parts[0];
        String tableName2 = parts[2];
        String column1 = parts[4];
        String column2 = parts[6];

        return database.join(tableName1, tableName2, column1, column2);
    }

    /**
     * Parse value list string
     */
    private String[] parseValues(String valuesStr) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentValue = new StringBuilder();

        for (int i = 0; i < valuesStr.length(); i++) {
            char c = valuesStr.charAt(i);

            if (c == '\'' || c == '"') {
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
        return values.toArray(new String[0]);
    }

    /**
     * Parse name-value pair list string
     */
    private List<String> parseNameValuePairs(String nameValueStr) {
        List<String> pairs = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentPair = new StringBuilder();

        for (int i = 0; i < nameValueStr.length(); i++) {
            char c = nameValueStr.charAt(i);

            if (c == '\'' || c == '"') {
                inQuotes = !inQuotes;
                currentPair.append(c);
            } else if (c == ',' && !inQuotes) {
                pairs.add(currentPair.toString().trim());
                currentPair = new StringBuilder();
            } else {
                currentPair.append(c);
            }
        }
        if (currentPair.length() > 0) {
            pairs.add(currentPair.toString().trim());
        }
        return pairs;
    }
}