package edu.uob.CommandParser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Supports comparison operators and logical operators (AND, OR).

public class ConditionParser {
    private static final Pattern COMPARISON_PATTERN =
            Pattern.compile("([a-zA-Z0-9_]+)\\s*(==|!=|>|<|>=|<=|LIKE)\\s*(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern LOGICAL_OPERATOR_PATTERN =
            Pattern.compile(".+(\\s+AND\\s+|\\s+OR\\s+).+", Pattern.CASE_INSENSITIVE);


    public static class ConditionNode {
        public String operator; // "AND", "OR", "==", ">", "<", ">=", "<=", "!=", "LIKE"
        public String attributeName;
        public String value;
        public ConditionNode left;
        public ConditionNode right;

        public ConditionNode(String operator, ConditionNode left, ConditionNode right) {
            this.operator = operator;
            this.left = left;
            this.right = right;
        }

        public ConditionNode(String operator, String attributeName, String value) {
            this.operator = operator;
            this.attributeName = attributeName;
            this.value = value;
        }

        // debug
        @Override
        public String toString() {
            if (left != null && right != null) {
                return "(" + left + " " + operator + " " + right + ")";
            } else {
                return attributeName + " " + operator + " " + value;
            }
        }
    }

    //Parse a condition string into a condition tree.
    public ConditionNode parse(String condition) {
        condition = condition.trim();

        // First check for logical operators (AND, OR)
        Matcher logicalMatcher = LOGICAL_OPERATOR_PATTERN.matcher(condition);
        if (logicalMatcher.matches()) {
            String operator = logicalMatcher.group(1).trim().toUpperCase();

            //  handle nested parentheses
            int splitIndex = findLogicalOperatorSplitIndex(condition, operator);
            if (splitIndex > 0) {
                String leftExpr = condition.substring(0, splitIndex).trim();
                String rightExpr = condition.substring(splitIndex + operator.length()).trim();

                ConditionNode leftNode = parse(leftExpr);
                ConditionNode rightNode = parse(rightExpr);

                return new ConditionNode(operator, leftNode, rightNode);
            }
        }

        // No logical operators parse a comparison condition
        return parseComparisonCondition(condition);
    }

    //Handles parentheses to ensure correct nesting.

    private int findLogicalOperatorSplitIndex(String condition, String operator) {
        int parenLevel = 0;
        int i = 0;

        while (i <= condition.length() - operator.length()) {
            char c = condition.charAt(i);

            if (c == '(') parenLevel++;
            else if (c == ')') parenLevel--;
            if (parenLevel == 0 &&
                    condition.substring(i, i + operator.length()).equalsIgnoreCase(operator)) {
                return i;
            }
            i++;
        }
        return -1;
    }

// Parse a simple comparison condition (no AND/OR).

    private ConditionNode parseComparisonCondition(String condition) {
        // Remove surrounding parentheses if present
        condition = removeParentheses(condition);

        Matcher matcher = COMPARISON_PATTERN.matcher(condition);

        if (matcher.find()) {
            String attribute = matcher.group(1).trim();
            String operator = matcher.group(2).trim();
            String value = matcher.group(3).trim();
            if (value.startsWith("'") && value.endsWith("'")) {
                value = value.substring(1, value.length() - 1);
            }

            return new ConditionNode(operator, attribute, value);
        }

        return null; // Unable to parse the condition
    }

// Remove surrounding parentheses while preserving nested ones.

    private String removeParentheses(String expr) {
        expr = expr.trim();

        if (expr.startsWith("(") && expr.endsWith(")")) {
            int parenLevel = 1;
            for (int i = 1; i < expr.length() - 1; i++) {
                if (expr.charAt(i) == '(') parenLevel++;
                else if (expr.charAt(i) == ')') parenLevel--;

                if (parenLevel == 0) return expr;
            }

            // Remove the outer parentheses and try again
            return removeParentheses(expr.substring(1, expr.length() - 1));
        }

        return expr;
    }
// Evaluate a condition node against a row of data.

    public boolean evaluate(ConditionNode node, String[] row, List<String> columnNames) {
        if (node == null) return true;
        // Logical operations (AND, OR)
        if (node.left != null && node.right != null) {
            boolean leftResult = evaluate(node.left, row, columnNames);
            boolean rightResult = evaluate(node.right, row, columnNames);
            if (node.operator.equalsIgnoreCase("AND")) {
                return leftResult && rightResult;
            } else if (node.operator.equalsIgnoreCase("OR")) {
                return leftResult || rightResult;
            }
        }

        // Comparison operations
        int colIndex = findColumnIndex(node.attributeName, columnNames);
        if (colIndex == -1 || colIndex >= row.length) return false;

        String rowValue = row[colIndex];

        // Handle different operators
        switch (node.operator.toUpperCase()) {
            case "==":
                return rowValue.equalsIgnoreCase(node.value);
            case "!=":
                return !rowValue.equalsIgnoreCase(node.value);
            case ">":
                return compareValues(rowValue, node.value) > 0;
            case "<":
                return compareValues(rowValue, node.value) < 0;
            case ">=":
                return compareValues(rowValue, node.value) >= 0;
            case "<=":
                return compareValues(rowValue, node.value) <= 0;
            case "LIKE":
                return matchesLikePattern(rowValue, node.value);
            default:
                return false;
        }
    }


    private int compareValues(String value1, String value2) {
        // Try first
        try {
            double num1 = Double.parseDouble(value1);
            double num2 = Double.parseDouble(value2);
            return Double.compare(num1, num2);
        } catch (NumberFormatException e) {
            // Fall back to string comparison
            return value1.compareToIgnoreCase(value2);
        }
    }

    //Find the index of a column by name.
    private int findColumnIndex(String columnName, List<String> columnNames) {
        for (int i = 0; i < columnNames.size(); i++) {
            if (columnNames.get(i).equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }

    //Supports % as wildcard.
    private boolean matchesLikePattern(String value, String pattern) {
        String regex = pattern.replace("%", ".*").replace("_", ".");
        return value.matches("(?i)" + regex);
    }
}