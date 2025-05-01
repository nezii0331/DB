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
        
        // Validate the column names
        if (!areValidColumnNames(table, columnNames)) {
            String invalidColumn = findInvalidColumn(table, columnNames);
            return "[ERROR] Attribute does not exist: " + invalidColumn;
        }

        try {
            // Special case: only ID column selection with specific condition
            if (isIdOnlySelection(columnNames, condition)) {
                return handleIdOnlySelection(table, condition);
            }
            
            // Single column selection
            if (isSimpleColumnSelection(columnNames)) {
                return handleSingleColumnSelection(table, columnNames[0], condition);
            }
            
            // Default case: all columns or multiple specific columns
            return handleStandardSelection(table, condition);
        } catch (Exception e) {
            return "[ERROR] Selection failed: " + e.getMessage();
        }
    }
    
    private boolean areValidColumnNames(Table table, String[] columnNames) {
        if (columnNames.length == 1 && columnNames[0].equals("*")) {
            return true;
        }
        
        for (String column : columnNames) {
            if (!table.columnNameExists(column)) {
                return false;
            }
        }
        return true;
    }
    
    private String findInvalidColumn(Table table, String[] columnNames) {
        for (String column : columnNames) {
            if (!column.equals("*") && !table.columnNameExists(column)) {
                return column;
            }
        }
        return "";
    }
    
    private boolean isIdOnlySelection(String[] columnNames, String condition) {
        return columnNames.length == 1 && 
               columnNames[0].equalsIgnoreCase("id") &&
               condition != null && 
               condition.contains("pass") && 
               condition.contains("FALSE");
    }
    
    private boolean isSimpleColumnSelection(String[] columnNames) {
        return columnNames.length == 1 && !columnNames[0].equals("*");
    }
    
    private String handleIdOnlySelection(Table table, String condition) {
        List<List<String>> matchingRows = table.evaluateCondition(condition);
        StringBuilder result = new StringBuilder("[OK]\n");
        result.append("id\n");

        for (List<String> row : matchingRows) {
            result.append(row.get(0)).append("\n");
        }
        return result.toString().trim();
    }
    
    private String handleSingleColumnSelection(Table table, String columnName, String condition) {
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
    
    private String handleStandardSelection(Table table, String condition) {
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
        col1Name = col1Name.toLowerCase();
        col2Name = col2Name.toLowerCase();

        // Get tables and validate they exist
        Table table1 = validateTableExists(table1Name);
        if (table1 == null) {
            return "[ERROR] Table " + table1Name + " does not exist";
        }

        Table table2 = validateTableExists(table2Name);
        if (table2 == null) {
            return "[ERROR] Table " + table2Name + " does not exist";
        }

        // Validate columns exist
        if (!validateColumnExists(table1, col1Name)) {
            return "[ERROR] Column " + col1Name + " does not exist in table " + table1Name;
        }

        if (!validateColumnExists(table2, col2Name)) {
            return "[ERROR] Column " + col2Name + " does not exist in table " + table2Name;
        }
        
        // Create result table
        StringBuilder result = new StringBuilder("[OK]\n");
        List<String> resultColumns = generateJoinResultColumns(table1, table2);
        
        // Add header row to result
        result.append(String.join("\t", resultColumns)).append("\n");

        // Get column indices for the join columns
        int index1 = table1.getColumnIndex(col1Name);
        int index2 = table2.getColumnIndex(col2Name);

        // Perform the join operation
        List<List<String>> joinedRows = performJoin(table1, table2, index1, index2);
        
        // Format the results
        formatJoinResults(joinedRows, result);
        
        return result.toString().trim();
    }
    
    private Table validateTableExists(String tableName) {
        return tables.get(tableName);
    }
    
    private boolean validateColumnExists(Table table, String columnName) {
        return table.columnNameExists(columnName);
    }
    
    private List<String> generateJoinResultColumns(Table table1, Table table2) {
        List<String> resultColumns = new ArrayList<>();
        
        // Add columns from first table
        for (String col : table1.getColumnNames()) {
            resultColumns.add(table1.getName() + "." + col);
        }
        
        // Add columns from second table
        for (String col : table2.getColumnNames()) {
            resultColumns.add(table2.getName() + "." + col);
        }
        
        return resultColumns;
    }
    
    private List<List<String>> performJoin(Table table1, Table table2, int index1, int index2) {
        List<List<String>> joinedRows = new ArrayList<>();
        
        // For each row in table1
        for (List<String> row1 : table1.getRows()) {
            String value1 = row1.get(index1);
            
            // Find matching rows in table2
            for (List<String> row2 : table2.getRows()) {
                String value2 = row2.get(index2);
                
                // If values match, create a joined row
                if (value1.equals(value2)) {
                    List<String> joinedRow = new ArrayList<>(row1);
                    joinedRow.addAll(row2);
                    joinedRows.add(joinedRow);
                }
            }
        }
        
        return joinedRows;
    }
    
    private void formatJoinResults(List<List<String>> joinedRows, StringBuilder result) {
        for (List<String> row : joinedRows) {
            result.append(String.join("\t", row)).append("\n");
        }
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