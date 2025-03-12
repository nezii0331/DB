package edu.uob.CommandParser;

/**
 * A class for parsing and converting values.
 * Handles string literals, boolean values, NULL, integers, and floating numbers.
 */
public class ValueParser {

    public static Object parseValue(String valueStr) {
        if (valueStr == null) {
            return null;
        }
        valueStr = valueStr.trim();

        // Empty string case
        if (valueStr.isEmpty()) {
            return "";
        }

        // String literal (quoted value)
        if (valueStr.startsWith("'") && valueStr.endsWith("'")) {
            return valueStr.substring(1, valueStr.length() - 1);
        }

        // Boolean values
        if (valueStr.equalsIgnoreCase("TRUE")) {
            return Boolean.TRUE;
        } else if (valueStr.equalsIgnoreCase("FALSE")) {
            return Boolean.FALSE;
        }

        // NULL value
        if (valueStr.equalsIgnoreCase("NULL")) {
            return null;
        }

        // Floating number
        if (valueStr.matches("[+-]?\\d+\\.\\d+")) {
            try {
                return Double.parseDouble(valueStr);
            } catch (NumberFormatException e) {
                return valueStr;
            }
        }

        // Integer
        if (valueStr.matches("[+-]?\\d+")) {
            try {
                return Integer.parseInt(valueStr);
            } catch (NumberFormatException e) {
                try {
                    return Long.parseLong(valueStr);
                } catch (NumberFormatException e2) {
                    return valueStr;
                }
            }
        }
        return valueStr;
    }

    public static String valueToString(Object value) {
        if (value == null) {
            return "NULL";
        } else if (value instanceof String) {
            return "'" + value + "'";
        } else {
            return value.toString();
        }
    }

    public static boolean valuesEqual(Object value1, Object value2) {
        // Handle null cases
        if (value1 == null && value2 == null) {
            return true;
        }
        if (value1 == null || value2 == null) {
            return false;
        }
        // If both are numbers, compare numerically
        if (isNumber(value1) && isNumber(value2)) {
            double num1 = ((Number) value1).doubleValue();
            double num2 = ((Number) value2).doubleValue();
            return Math.abs(num1 - num2) < 0.0000001;
        }
        return value1.equals(value2);
    }


    //Check if Integer, Long, Float, Double
    private static boolean isNumber(Object value) {
        return value instanceof Number;
    }

    //handling different types appropriately.
    public static int compareValues(Object value1, Object value2) {
        if (value1 == null && value2 == null) {
            return 0;
        }
        if (value1 == null) {
            return -1;
        }
        if (value2 == null) {
            return 1;
        }
        if (isNumber(value1) && isNumber(value2)) {
            double num1 = ((Number) value1).doubleValue();
            double num2 = ((Number) value2).doubleValue();
            return Double.compare(num1, num2);
        }
        if (value1 instanceof String && value2 instanceof String) {
            return ((String) value1).compareTo((String) value2);
        }
        // If types are different convert
        return value1.toString().compareTo(value2.toString());
    }
}
