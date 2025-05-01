package edu.uob.DataStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


/**
 * Represents a database table
 */

public class Table {
    private String name;
    private List<List<String>> rows;
    private List<String> columnNames;
    private int nextId = 1;
    private int maxUsedId = 0;  // Track the highest ID ever used

    public Table(String name) {
        this.name = name;
        this.rows = new ArrayList<>();
        this.columnNames = new ArrayList<>();
        // Only one id
        columnNames.add("id");
    }

    // Add a column
    public boolean addColumn(String columnName) {
        if (columnNameExists(columnName)) {
            return false;
        }
        columnNames.add(columnName);
        for (List<String> row : rows) {
            row.add("NULL");
        }
        return true;
    }


    // Check if column name exists
    public boolean columnNameExists(String columnName) {
        for (String col : columnNames) {
            if (col.equals(columnName)) {
                return true;
            }
        }
        return false;
    }

    // Get column index
    public int getColumnIndex(String columnName) {
        for (int i = 0; i < columnNames.size(); i++) {
            if (columnNames.get(i).equals(columnName)) {
                return i;
            }
        }
        return -1;
    }

    // Delete a column
    public boolean dropColumn(String columnName) {
        // Cannot delete id column
        if (columnName.equalsIgnoreCase("id")) {
            return false;
        }
        int colIndex = getColumnIndex(columnName);
        if (colIndex == -1) {
            return false;
        }
        columnNames.remove(colIndex);
        for (List<String> row : rows) {
            row.remove(colIndex);
        }
        return true;
    }


    // Load rows directly from data columns
    public void addRowDirect(List<String> rowData) {
        List<String> adjustedRow = new ArrayList<>();
        if (rowData.isEmpty()) {
            adjustedRow.add(String.valueOf(nextId++));
            maxUsedId = Math.max(maxUsedId, nextId - 1);
        } else {
            adjustedRow.add(rowData.get(0));
            try {
                int id = Integer.parseInt(rowData.get(0));
                if (id >= nextId) {
                    nextId = id + 1;
                }
                maxUsedId = Math.max(maxUsedId, id);
            } catch (NumberFormatException ignored) {
            }
        }
        for (int i = 1; i < Math.min(rowData.size(), columnNames.size()); i++) {
            adjustedRow.add(rowData.get(i));
        }
        while (adjustedRow.size() < columnNames.size()) {
            adjustedRow.add("NULL");
        }

        rows.add(adjustedRow);
    }

    // Add a row of data
    public void addRow(String[] values) {
        // Check for too few values - need at least one value for non-ID columns
        if (values.length < 1 && columnNames.size() > 1) {
            throw new IllegalArgumentException("Too few values provided for insertion");
        }
        
        if (values.length > columnNames.size() - 1) { // id column already exists, so -1
            throw new IllegalArgumentException("Too many values");
        }
        List<String> newRow = new ArrayList<>();
        
        // Use max of nextId and maxUsedId+1 to ensure IDs are never recycled
        int idToUse = Math.max(nextId, maxUsedId + 1);
        newRow.add(String.valueOf(idToUse));
        nextId = idToUse + 1;
        maxUsedId = idToUse;
        
        for (int i = 0; i < values.length; i++) {
            String value = values[i].trim();
            if (value.startsWith("'") && value.endsWith("'")) {
                value = value.substring(1, value.length() - 1);
            }
            if (value.equalsIgnoreCase("TRUE")) {
                value = "TRUE";
            } else if (value.equalsIgnoreCase("FALSE")) {
                value = "FALSE";
            } else if (value.equalsIgnoreCase("NULL")) {
                value = "NULL";
            }

            newRow.add(value);
        }
        while (newRow.size() < columnNames.size()) {
            newRow.add("NULL");
        }
        rows.add(newRow);
    }



    // Update rows
    public int updateRows(String columnName, String newValue, String condition) {
        int colIndex = getColumnIndex(columnName);
        if (colIndex == -1) {
            return 0;
        }
        
        // Prevent changing ID column
        if (columnName.equalsIgnoreCase("id")) {
            throw new IllegalArgumentException("Cannot modify the ID column");
        }
        
        if (newValue.startsWith("'") && newValue.endsWith("'")) {
            newValue = newValue.substring(1, newValue.length() - 1);
        }
        if (newValue.equalsIgnoreCase("TRUE")) {
            newValue = "TRUE";
        } else if (newValue.equalsIgnoreCase("FALSE")) {
            newValue = "FALSE";
        }
        List<List<String>> matchingRows = evaluateCondition(condition);
        for (List<String> row : matchingRows) {
            row.set(colIndex, newValue);
        }
        return matchingRows.size();
    }

    // Delete rows
    public boolean deleteRows(String condition) {
        if (condition == null || condition.trim().isEmpty()) {
            return false; // No unconditional deletion allowed
        }
        List<List<String>> rowsToDelete = evaluateCondition(condition);
        if (rowsToDelete.isEmpty()) {
            return true; // No matching rows is still considered success
        }
        rows.removeAll(rowsToDelete);
        return true;
    }

    // Calculate matching rows based on condition
    public List<List<String>> evaluateCondition(String condition) {
        // If no condition, return all rows
        if (condition == null || condition.trim().isEmpty()) {
            return new ArrayList<>(rows);
        }
        
        // Normalize the condition
        condition = normalizeCondition(condition);
        
        // Handle AND operator
        if (containsLogicalOperator(condition, " AND ")) {
            return evaluateAndCondition(condition);
        }

        // Handle OR operator
        if (containsLogicalOperator(condition, " OR ")) {
            return evaluateOrCondition(condition);
        }

        // Simple condition - no AND/OR
        return evaluateSimpleCondition(condition, rows);
    }
    
    private String normalizeCondition(String condition) {
        condition = condition.trim();
        // Remove outermost parentheses if they're balanced
        if (condition.startsWith("(") && condition.endsWith(")") &&
                countMatchingBrackets(condition) == 1) {
            return condition.substring(1, condition.length() - 1).trim();
        }
        return condition;
    }
    
    private boolean containsLogicalOperator(String condition, String operator) {
        String[] parts = splitOutsideBrackets(condition, operator);
        return parts.length == 2;
    }
    
    private List<List<String>> evaluateAndCondition(String condition) {
        String[] parts = splitOutsideBrackets(condition, " AND ");
        if (parts.length == 2) {
            List<List<String>> leftResult = evaluateCondition(parts[0].trim());
            List<List<String>> rightResult = evaluateCondition(parts[1].trim());
            leftResult.retainAll(rightResult); // intersection
            return leftResult;
        }
        // Shouldn't happen if containsLogicalOperator was called first
        return new ArrayList<>();
    }
    
    private List<List<String>> evaluateOrCondition(String condition) {
        String[] parts = splitOutsideBrackets(condition, " OR ");
        if (parts.length == 2) {
            List<List<String>> leftResult = evaluateCondition(parts[0].trim());
            List<List<String>> rightResult = evaluateCondition(parts[1].trim());

            // Create the union - keep all rows from left and add non-duplicate rows from right
            List<List<String>> result = new ArrayList<>(leftResult);
            for (List<String> row : rightResult) {
                if (!containsRow(result, row)) {
                    result.add(row);
                }
            }
            return result;
        }
        // Shouldn't happen if containsLogicalOperator was called first
        return new ArrayList<>();
    }

    // Helper method: Count matching bracket pairs in a condition string
    private int countMatchingBrackets(String condition) {
        int count = 0;
        int level = 0;

        for (char c : condition.toCharArray()) {
            if (c == '(') {
                if (level == 0) {
                    count++;
                }
                level++;
            } else if (c == ')') {
                level--;
            }
        }

        return count;
    }

    // Helper method
    private String[] splitOutsideBrackets(String str, String delimiter) {
        List<String> result = new ArrayList<>();
        int start = 0;
        int level = 0;

        for (int i = 0; i < str.length() - delimiter.length() + 1; i++) {
            // Check bracket level
            if (str.charAt(i) == '(') {
                level++;
            } else if (str.charAt(i) == ')') {
                level--;
            }
            if (level == 0 && str.substring(i, i + delimiter.length()).equals(delimiter)) {
                result.add(str.substring(start, i).trim());
                start = i + delimiter.length();
                i += delimiter.length() - 1;
            }
        }

        if (start < str.length()) {
            result.add(str.substring(start).trim());
        }
        return result.toArray(new String[0]);
    }

    // Helper method
    private boolean containsRow(List<List<String>> rows, List<String> row) {
        for (List<String> existingRow : rows) {
            if (existingRow.get(0).equals(row.get(0))) { // Compare ID column
                return true;
            }
        }
        return false;
    }

    // Handle simple conditions
    private List<List<String>> evaluateSimpleCondition(String condition, List<List<String>> rowsToFilter) {
        List<List<String>> matchingRows = new ArrayList<>();
        
        // Extract column name and value from condition
        Map<String, Object> extractedCondition = extractConditionParts(condition);
        if (extractedCondition == null) {
            return rowsToFilter; // Invalid condition, return original rows
        }
        
        String columnName = (String) extractedCondition.get("column");
        String operator = (String) extractedCondition.get("operator");
        String value = (String) extractedCondition.get("value");
        
        // Get column index
        int columnIndex = getColumnIndex(columnName);
        if (columnIndex == -1) {
            return new ArrayList<>(); // Column not found, return empty result
        }
        
        // Check each row for a match
        for (List<String> row : rowsToFilter) {
            if (row.size() > columnIndex) {
                String cellValue = row.get(columnIndex);
                if (evaluateComparison(cellValue, operator, value)) {
                    matchingRows.add(row);
                }
            }
        }
        
        return matchingRows;
    }
    
    private Map<String, Object> extractConditionParts(String condition) {
        // Parse simple conditions like: columnName == value
        String[] operators = {"==", "!=", ">=", "<=", ">", "<", "LIKE"};
        
        for (String operator : operators) {
            if (condition.contains(operator)) {
                String[] parts = condition.split(operator, 2);
                if (parts.length == 2) {
                    String columnName = parts[0].trim().toLowerCase();
                    String value = parts[1].trim();
                    
                    // Remove surrounding quotes if present
                    if (value.startsWith("'") && value.endsWith("'")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("column", columnName);
                    result.put("operator", operator);
                    result.put("value", value);
                    
                    return result;
                }
            }
        }
        
        return null; // Invalid condition format
    }

    // Calculate comparison result between two values
    private boolean evaluateComparison(String cellValue, String operator, String value) {
        // Handle NULL values
        if (cellValue.equals("NULL") || value.equals("NULL")) {
            return false; // Comparisons with NULL always return false
        }

        // Check if we're comparing a number with a string
        boolean cellIsNumber = isNumeric(cellValue);
        boolean valueIsNumber = isNumeric(value);
        
        // If one is a number and the other is a string (and not a boolean), return false for incompatible types
        if ((cellIsNumber && !valueIsNumber && !isBoolean(value)) || 
            (valueIsNumber && !cellIsNumber && !isBoolean(cellValue))) {
            return false;  // Incompatible types comparison should return empty result
        }

        switch (operator) {
            case "==":
                return cellValue.equalsIgnoreCase(value);
            case "!=":
                return !cellValue.equalsIgnoreCase(value);
            case ">":
                // Both numeric - do numeric comparison
                if (cellIsNumber && valueIsNumber) {
                    try {
                        return Double.parseDouble(cellValue) > Double.parseDouble(value);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
                // Both boolean values
                if (isBoolean(cellValue) && isBoolean(value)) {
                    return Boolean.parseBoolean(cellValue) && !Boolean.parseBoolean(value);
                }
                // String comparison
                return cellValue.compareTo(value) > 0;
            case "<":
                // Both numeric - do numeric comparison
                if (cellIsNumber && valueIsNumber) {
                    try {
                        return Double.parseDouble(cellValue) < Double.parseDouble(value);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
                // Both boolean values
                if (isBoolean(cellValue) && isBoolean(value)) {
                    return !Boolean.parseBoolean(cellValue) && Boolean.parseBoolean(value);
                }
                // String comparison
                return cellValue.compareTo(value) < 0;
            case ">=":
                // Both numeric - do numeric comparison
                if (cellIsNumber && valueIsNumber) {
                    try {
                        return Double.parseDouble(cellValue) >= Double.parseDouble(value);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
                // Both boolean values
                if (isBoolean(cellValue) && isBoolean(value)) {
                    return Boolean.parseBoolean(cellValue) || !Boolean.parseBoolean(value);
                }
                // String comparison
                return cellValue.compareTo(value) >= 0;
            case "<=":
                // Both numeric - do numeric comparison
                if (cellIsNumber && valueIsNumber) {
                    try {
                        return Double.parseDouble(cellValue) <= Double.parseDouble(value);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
                // Both boolean values
                if (isBoolean(cellValue) && isBoolean(value)) {
                    return !Boolean.parseBoolean(cellValue) || Boolean.parseBoolean(value);
                }
                // String comparison
                return cellValue.compareTo(value) <= 0;
            case "LIKE":
                return cellValue.matches(value.replace("%", ".*"));
            default:
                return false;
        }
    }
    
    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }
    
    private boolean isBoolean(String str) {
        return str.equalsIgnoreCase("TRUE") || str.equalsIgnoreCase("FALSE");
    }

    // Get column names
    public List<String> getColumnNames() {
        return columnNames;
    }

    // Get all rows
    public List<List<String>> getRows() {
        return new ArrayList<>(rows);
    }
    public List<String> getColumnValues(String columnName) {
        int colIndex = getColumnIndex(columnName);
        if (colIndex == -1) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (List<String> row : rows) {
            result.add(row.get(colIndex));
        }

        return result;
    }


    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join("\t", columnNames)).append("\n");
        for (List<String> row : rows) {
            sb.append(String.join("\t", row)).append("\n");
        }
        return sb.toString();
    }

    // Get table name
    public String getName() {
        return name;
    }
}
