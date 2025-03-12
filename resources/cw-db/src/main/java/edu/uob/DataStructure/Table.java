package edu.uob.DataStructure;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a database table
 */

public class Table {
    private String name;
    private List<List<String>> rows;
    private List<String> columnNames;
    private int nextId = 1;

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
        } else {
            adjustedRow.add(rowData.get(0));
            try {
                int id = Integer.parseInt(rowData.get(0));
                if (id >= nextId) {
                    nextId = id + 1;
                }
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
        if (values.length > columnNames.size() - 1) { // id column already exists, so -1
            throw new IllegalArgumentException("Too many values");
        }
        List<String> newRow = new ArrayList<>();
        newRow.add(String.valueOf(nextId++));
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
        if (condition == null || condition.trim().isEmpty()) {
            return new ArrayList<>(rows);
        }
        condition = condition.trim();
        if (condition.startsWith("(") && condition.endsWith(")") &&
                countMatchingBrackets(condition) == 1) {
            condition = condition.substring(1, condition.length() - 1).trim();
        }
        // AND
        if (condition.contains(" AND ")) {
            String[] parts = splitOutsideBrackets(condition, " AND ");
            if (parts.length == 2) {
                List<List<String>> leftResult = evaluateCondition(parts[0].trim());
                List<List<String>> rightResult = evaluateCondition(parts[1].trim());
                leftResult.retainAll(rightResult);
                return leftResult;
            }
        }

        // OR
        if (condition.contains(" OR ")) {
            String[] parts = splitOutsideBrackets(condition, " OR ");
            if (parts.length == 2) {
                List<List<String>> leftResult = evaluateCondition(parts[0].trim());
                List<List<String>> rightResult = evaluateCondition(parts[1].trim());

                // 取并集 - 保留任一边匹配的行
                List<List<String>> result = new ArrayList<>(leftResult);
                for (List<String> row : rightResult) {
                    if (!containsRow(result, row)) {
                        result.add(row);
                    }
                }
                return result;
            }
        }
        return evaluateSimpleCondition(condition, rows);
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
    private List<List<String>> evaluateSimpleCondition(String condition, List<List<String>> rows) {
        List<List<String>> result = new ArrayList<>();
        
        // Handle conditions in parentheses
        if (condition.startsWith("(") && condition.endsWith(")")) {
            condition = condition.substring(1, condition.length() - 1).trim();
        }

        // Extract condition parts
        String attributeName = "";
        String operator = "";
        String value = "";

        if (condition.contains("==")) {
            String[] parts = condition.split("==");
            attributeName = parts[0].trim();
            operator = "==";
            value = parts[1].trim();
        } else if (condition.contains("!=")) {
            String[] parts = condition.split("!=");
            attributeName = parts[0].trim();
            operator = "!=";
            value = parts[1].trim();
        } else if (condition.contains(">=")) {
            String[] parts = condition.split(">=");
            attributeName = parts[0].trim();
            operator = ">=";
            value = parts[1].trim();
        } else if (condition.contains("<=")) {
            String[] parts = condition.split("<=");
            attributeName = parts[0].trim();
            operator = "<=";
            value = parts[1].trim();
        } else if (condition.contains(">")) {
            String[] parts = condition.split(">");
            attributeName = parts[0].trim();
            operator = ">";
            value = parts[1].trim();
        } else if (condition.contains("<")) {
            String[] parts = condition.split("<");
            attributeName = parts[0].trim();
            operator = "<";
            value = parts[1].trim();
        } else if (condition.contains("LIKE")) {
            String[] parts = condition.split("LIKE");
            attributeName = parts[0].trim();
            operator = "LIKE";
            value = parts[1].trim();
        } else {
            return result; // Cannot parse condition, return empty list
        }
        // Remove quotes from values
        if (value.startsWith("'") && value.endsWith("'")) {
            value = value.substring(1, value.length() - 1);
        }

        int colIndex = getColumnIndex(attributeName);
        if (colIndex == -1) {
            return result; // Return empty list if column doesn't exist
        }
        for (List<String> row : rows) {
            String cellValue = row.get(colIndex);
            if (evaluateComparison(cellValue, operator, value)) {
                result.add(row);
            }
        }
        return result;
    }

    // Calculate comparison result between two values
    private boolean evaluateComparison(String cellValue, String operator, String value) {
        // Handle NULL value comparisons
        if (value.equalsIgnoreCase("NULL")) {
            switch (operator) {
                case "==":
                    return cellValue.equalsIgnoreCase("NULL");
                case "!=":
                    return !cellValue.equalsIgnoreCase("NULL");
                default:
                    return false;
            }
        }

        // Handle normal value comparisons
        switch (operator) {
            case "==":
                return cellValue.equals(value);
            case "!=":
                return !cellValue.equals(value);
            case ">":
                try {
                    double cellNum = Double.parseDouble(cellValue);
                    double valueNum = Double.parseDouble(value);
                    return cellNum > valueNum;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "<":
                try {
                    double cellNum = Double.parseDouble(cellValue);
                    double valueNum = Double.parseDouble(value);
                    return cellNum < valueNum;
                } catch (NumberFormatException e) {
                    return false;
                }
            case ">=":
                try {
                    double cellNum = Double.parseDouble(cellValue);
                    double valueNum = Double.parseDouble(value);
                    return cellNum >= valueNum;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "<=":
                try {
                    double cellNum = Double.parseDouble(cellValue);
                    double valueNum = Double.parseDouble(value);
                    return cellNum <= valueNum;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "LIKE":
                return cellValue.contains(value);
            default:
                return false;
        }
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
