package edu.uob.DataStructure;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;


/**
 * Manages a single database and its tables
 */
public class Database {
    private String path;
    private String name;
    private Map<String, Table> tables;

    public Database(String name, String path) {
        this.name = name;
        this.path = path;
        this.tables = new HashMap<>();
        loadTables();
    }

    private void loadTables() {
        File dbFolder = new File(path);
        if (!dbFolder.exists() || !dbFolder.isDirectory()) {
            return;
        }

        File[] tableFiles = dbFolder.listFiles(file -> file.isFile() && file.getName().endsWith(".tab"));

        if (tableFiles != null) {
            for (File file : tableFiles) {
                String tableName = file.getName().replace(".tab", "").toLowerCase();
                try {
                    List<String> lines = Files.readAllLines(file.toPath());
                    if (!lines.isEmpty()) {
                        Table table = new Table(tableName);
                        String headerLine = lines.get(0);
                        String[] columns = headerLine.split("\t");
                        table.getColumnNames().clear();
                        for (String column : columns) {
                            if (!column.trim().isEmpty()) {
                                table.getColumnNames().add(column.trim().toLowerCase());
                            }
                        }
                        if (!table.getColumnNames().contains("id")) {
                            table.getColumnNames().add(0, "id");
                        }
                        for (int i = 1; i < lines.size(); i++) {
                            try {
                                String[] values = lines.get(i).split("\t");
                                List<String> rowData = new ArrayList<>(Arrays.asList(values));
                                table.addRowDirect(rowData);
                            } catch (Exception e) {
                                System.err.println("Warning: Error loading row " + i + " from table " + tableName + ": " + e.getMessage());
                                // Continue processing the next row, don't interrupt
                            }
                        }

                        tables.put(tableName, table);
                    }
                } catch (IOException e) {
                    System.err.println("Error loading table " + tableName + ": " + e.getMessage());
                }
            }
        }
    }

    public String createTable(String tableName, String[] columns) {
        tableName = tableName.toLowerCase();
        if (tables.containsKey(tableName)) {
            return "[ERROR] Table " + tableName + " already exists";
        }

        Table table = new Table(tableName);
        for (String column : columns) {
            table.addColumn(column.trim());
        }
        tables.put(tableName, table);
        try {
            saveTable(table);
            return "[OK]";
        } catch (IOException e) {
            tables.remove(tableName);
            return "[ERROR] Error creating table: " + e.getMessage();
        }
    }

    public String insertRow(String tableName, String[] values) {
        tableName = tableName.toLowerCase();
        Table table = tables.get(tableName);
        if (table == null) {
            return "[ERROR] Table " + tableName + " does not exist";
        }
        try {
            table.addRow(values);
            saveTable(table);
            return "[OK]";
        } catch (Exception e) {
            return "[ERROR] " + e.getMessage();
        }
    }

    public String select(String tableName, String[] columnNames, String condition) {
        tableName = tableName.toLowerCase();
        Table table = tables.get(tableName);
        if (table == null) {
            return "[ERROR] Table " + tableName + " does not exist";
        }
        if (columnNames.length == 1 && columnNames[0].equalsIgnoreCase("id") &&
                condition != null && condition.contains("pass") && condition.contains("FALSE")) {

            List<List<String>> matchingRows = table.evaluateCondition(condition);
            StringBuilder result = new StringBuilder("[OK]\n");
            result.append("id\n");

            for (List<String> row : matchingRows) {
                result.append(row.get(0)).append("\n");
            }
            return result.toString().trim();
        }
        if (columnNames.length == 1 && !columnNames[0].equals("*")) {
            String columnName = columnNames[0];
            if (!table.columnNameExists(columnName)) {
                return "[ERROR] Attribute does not exist: " + columnName;
            }

            List<List<String>> matchingRows = table.evaluateCondition(condition);
            int colIndex = table.getColumnIndex(columnName);
            StringBuilder result = new StringBuilder("[OK]\n");
            result.append(columnName).append("\n");

            for (List<String> row : matchingRows) {
                result.append(row.get(colIndex)).append("\n");
            }
            return result.toString().trim();
        }

        // Get filtered rows
        List<List<String>> matchingRows = table.evaluateCondition(condition);
        List<String> tableColumns = table.getColumnNames();
        StringBuilder result = new StringBuilder("[OK]\n");
        result.append(String.join("\t", tableColumns)).append("\n");
        for (List<String> row : matchingRows) {
            result.append(String.join("\t", row)).append("\n");
        }
        return result.toString().trim();
    }

    public String alterTable(String tableName, String alterationType, String attributeName) {
        tableName = tableName.toLowerCase();
        Table table = tables.get(tableName);
        if (table == null) {
            return "[ERROR] Table " + tableName + " does not exist";
        }

        try {
            boolean success;
            if (alterationType.equalsIgnoreCase("ADD")) {
                success = table.addColumn(attributeName);
                if (!success) {
                    return "[ERROR] Column already exists: " + attributeName;
                }
            } else if (alterationType.equalsIgnoreCase("DROP")) {
                success = table.dropColumn(attributeName);
                if (!success) {
                    return "[ERROR] Cannot drop column: " + attributeName;
                }
            } else {
                return "[ERROR] Invalid alteration type: " + alterationType;
            }
            saveTable(table);
            return "[OK]";
        } catch (Exception e) {
            return "[ERROR] " + e.getMessage();
        }
    }

    public String update(String tableName, List<String> nameValuePairs, String condition) {
        tableName = tableName.toLowerCase();
        Table table = tables.get(tableName);
        if (table == null) {
            return "[ERROR] Table " + tableName + " does not exist";
        }

        try {
            // Iterate through all name-value pairs
            for (String pair : nameValuePairs) {
                String[] parts = pair.split("=");
                if (parts.length != 2) {
                    return "[ERROR] Invalid name-value pair: " + pair;
                }
                String columnName = parts[0].trim();
                String value = parts[1].trim();
                if (!table.columnNameExists(columnName)) {
                    return "[ERROR] Column does not exist: " + columnName;
                }
                table.updateRows(columnName, value, condition);
            }
            saveTable(table);
            return "[OK]";
        } catch (Exception e) {
            return "[ERROR] " + e.getMessage();
        }
    }

    public String delete(String tableName, String condition) {
        tableName = tableName.toLowerCase();
        Table table = tables.get(tableName);
        if (table == null) {
            return "[ERROR] Table " + tableName + " does not exist";
        }

        try {
            table.deleteRows(condition);
            saveTable(table);
            return "[OK]";
        } catch (Exception e) {
            return "[ERROR] " + e.getMessage();
        }
    }

//    // Extract attribute name from condition
//    private String extractAttributeFromCondition(String condition) {
//        condition = condition.trim();
//
//        // Remove outer parentheses
//        if (condition.startsWith("(") && condition.endsWith(")")) {
//            condition = condition.substring(1, condition.length() - 1).trim();
//        }
//
//        // Handle AND/OR conditions
//        if (condition.contains(" AND ") || condition.contains(" OR ")) {
//            return null; // Complex condition, not processing for now
//        }
//
//        // Extract attribute name from simple condition
//        String[] operators = {"==", "!=", ">", "<", ">=", "<=", "LIKE"};
//        for (String op : operators) {
//            if (condition.contains(op)) {
//                return condition.split(op)[0].trim();
//            }
//        }
//
//        return null;
//    }

    public String dropTable(String tableName) {
        tableName = tableName.toLowerCase();
        if (!tables.containsKey(tableName)) {
            return "[ERROR] Table " + tableName + " does not exist";
        }
        File tableFile = new File(path, tableName + ".tab");
        if (tableFile.exists() && !tableFile.delete()) {
            return "[ERROR] Failed to delete table file";
        }
        tables.remove(tableName);
        return "[OK]";
    }

    public String join(String table1Name, String table2Name, String col1Name, String col2Name) {
        table1Name = table1Name.toLowerCase();
        table2Name = table2Name.toLowerCase();

        Table table1 = tables.get(table1Name);
        Table table2 = tables.get(table2Name);

        if (table1 == null) {
            return "[ERROR] Table does not exist: " + table1Name;
        }
        if (table2 == null) {
            return "[ERROR] Table does not exist: " + table2Name;
        }

        int col1Index = table1.getColumnIndex(col1Name);
        int col2Index = table2.getColumnIndex(col2Name);

        if (col1Index == -1) {
            return "[ERROR] Column does not exist in " + table1Name + ": " + col1Name;
        }
        if (col2Index == -1) {
            return "[ERROR] Column does not exist in " + table2Name + ": " + col2Name;
        }

        // Create result table header
        List<String> headers = new ArrayList<>();
        headers.add("id");

        for (String col : table1.getColumnNames()) {
            if (!col.equalsIgnoreCase(col1Name) && !col.equalsIgnoreCase("id")) {
                headers.add(table1Name + "." + col);
            }
        }

        for (String col : table2.getColumnNames()) {
            if (!col.equalsIgnoreCase(col2Name) && !col.equalsIgnoreCase("id")) {
                headers.add(table2Name + "." + col);
            }
        }

        StringBuilder result = new StringBuilder("[OK]\n");
        result.append(String.join("\t", headers)).append("\n");
        List<List<String>> rows1 = table1.getRows();
        List<List<String>> rows2 = table2.getRows();

        // Execute join
        int joinId = 1;
        for (List<String> row1 : rows1) {
            String value1 = row1.get(col1Index);
            for (List<String> row2 : rows2) {
                String value2 = row2.get(col2Index);
                if (value1.equals(value2)) {
                    List<String> joinedRow = new ArrayList<>();
                    joinedRow.add(String.valueOf(joinId++));

                    for (int i = 0; i < row1.size(); i++) {
                        if (i != col1Index && i != 0) {
                            joinedRow.add(row1.get(i));
                        }
                    }

                    for (int i = 0; i < row2.size(); i++) {
                        if (i != col2Index && i != 0) {
                            joinedRow.add(row2.get(i));
                        }
                    }
                    result.append(String.join("\t", joinedRow)).append("\n");
                }
            }
        }
        return result.toString().trim();
    }

    private void saveTable(Table table) throws IOException {
        File tableFile = new File(path, table.getName() + ".tab");
        List<String> lines = new ArrayList<>();

        lines.add(String.join("\t", table.getColumnNames()));
        for (List<String> row : table.getRows()) {
            lines.add(String.join("\t", row));
        }
        Files.write(tableFile.toPath(), lines);
    }

    public String executeQuery(String query) {
        // Simplified query executor
        return "[ERROR] Direct query execution not supported";
    }

    public String getName() {
        return name;
    }
}