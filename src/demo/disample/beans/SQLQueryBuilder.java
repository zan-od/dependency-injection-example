package demo.disample.beans;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLQueryBuilder {

    final String AND = "And";
    final String OR  = "Or";
    final String NOT = "Not";

    final String GreaterOrEqual = "GreaterOrEqual";
    final String LessOrEqual    = "LessOrEqual";
    final String Greater        = "Greater";
    final String Less           = "Less";
    final String NotEqual       = "NotEqual";
    final String IsEqual        = "IsEqual";

    private final List<String> logicalOperators = new ArrayList<>();
    private final Map<String, String> logicalOperatorsMap = new HashMap<>();
    private final List<String> conditionalOperators = new ArrayList<>();
    private final Map<String, String> conditionalOperatorsMap = new HashMap<>();

    private enum ClauseType {
        FIELD,
        CONDITION,
        LOGICAL
    }

    private class Condition{
        String field;
        String operator;
        boolean negative;
    }

    SQLQueryBuilder(){
        logicalOperators.add(AND);
        logicalOperatorsMap.put(AND, "AND");

        logicalOperators.add(OR);
        logicalOperatorsMap.put(OR, "OR");

        logicalOperatorsMap.put(NOT, "NOT");

        // order is important!!
        conditionalOperators.add(GreaterOrEqual);
        conditionalOperatorsMap.put(GreaterOrEqual, ">=");

        conditionalOperators.add(LessOrEqual);
        conditionalOperatorsMap.put(LessOrEqual, "<=");

        conditionalOperators.add(Greater);
        conditionalOperatorsMap.put(Greater, ">");

        conditionalOperators.add(Less);
        conditionalOperatorsMap.put(Less, "<");

        conditionalOperators.add(NotEqual);
        conditionalOperatorsMap.put(NotEqual, "<>");

        conditionalOperators.add(IsEqual);
        conditionalOperatorsMap.put(IsEqual, "=");
    }

    public static String buildUpdateQuery(String tableName, Map<String, String> fields){
        if (fields == null)
            throw new IllegalArgumentException("Field set is null");

        if (fields.size() == 0)
            throw new IllegalArgumentException("Field set is empty");

        StringBuilder values = new StringBuilder();
        boolean first = true;
        for (Map.Entry entry: fields.entrySet()) {
            if (!first){
                values.append(", ");
            }
            first = false;

            values.append(String.format("%s = '%s'", entry.getKey().toString(), entry.getValue().toString()));
        }

        return  String.format("UPDATE %s SET %s", tableName, values) ;
    }

    public static String buildInsertQuery(String tableName, Map<String, String> fields){
        if (fields == null)
            throw new IllegalArgumentException("Field set is null");

        if (fields.size() == 0)
            throw new IllegalArgumentException("Field set is empty");

        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        boolean first = true;
        for (Map.Entry entry: fields.entrySet()) {
            if (!first){
                columns.append(", ");
                values.append(", ");
            }
            first = false;

            columns.append(entry.getKey());
            values.append("'").append(entry.getValue()).append("'");
        }

        return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns.toString(), values.toString());
    }

    public String parseQuery(String methodName, Map<String, String> fieldsToColumns, Object[] values) throws ParseException {
        String methodMagicWord = "findBy";
        if (!methodName.startsWith(methodMagicWord)) {
            throw new IllegalArgumentException(String.format("Error in '%s' method name. Method name must start with '%s'", methodName, methodMagicWord));
        }

        Map<String, String> fields = new HashMap<>();
        for (Map.Entry entry: fieldsToColumns.entrySet()) {
            fields.put(Utils.capitalizeWord(entry.getKey().toString()), entry.getValue().toString());
        }

        int valueIndex  = -1;
        int currentPosition = methodMagicWord.length();
        int methodLength = methodName.length();

        StringBuilder queryBuilder = new StringBuilder();

        ClauseType expectedType = ClauseType.FIELD;
        Condition currentCondition = null;
        while (currentPosition < methodLength){
            if (expectedType == ClauseType.FIELD){
                currentCondition = new Condition();
                String currentFieldName = null;
                for (Map.Entry entry: fields.entrySet()) {
                    String fieldName = entry.getKey().toString();
                    if (methodName.startsWith (fieldName, currentPosition)){
                        currentFieldName = fieldName;
                        currentPosition += fieldName.length();
                        break;
                    }
                }

                if (currentFieldName == null){
                    throw newParseException("Error parsing method name ", methodName, " - field name expected", currentPosition);
                }

                currentCondition.field = currentFieldName;

                expectedType = ClauseType.CONDITION;

            } else if (expectedType == ClauseType.CONDITION){
                if (methodName.startsWith(NOT, currentPosition)
                        && (!methodName.startsWith(NotEqual, currentPosition))){
                    currentCondition.negative = true;
                    currentPosition += NOT.length();
                }

                String currentConditionOperator = null;
                for (String condition: conditionalOperators) {
                    if (methodName.startsWith (condition, currentPosition)){
                        currentConditionOperator = condition;
                        currentPosition += currentConditionOperator.length();
                        break;
                    }
                }

                currentCondition.operator = currentConditionOperator;

                expectedType = ClauseType.LOGICAL;

            } else if (expectedType == ClauseType.LOGICAL){
                if (currentCondition == null)
                    throw newParseException("Error parsing method name ", methodName, " - previous condition is null", currentPosition);
                if (currentCondition.field == null)
                    throw newParseException("Error parsing method name ", methodName, " - previous condition's field is empty", currentPosition);

                valueIndex = addCondition(queryBuilder, currentCondition, fields, values, valueIndex);
                currentCondition = null;

                String currentLogicalOperator = null;
                for (String logicalOperator: logicalOperators) {
                    if (methodName.startsWith(logicalOperator, currentPosition)){
                        currentLogicalOperator = logicalOperator;
                        currentPosition += currentLogicalOperator.length();
                        break;
                    }
                }

                if (currentLogicalOperator == null){
                    throw newParseException("Error parsing method name ", methodName, " - logical operator expected", currentPosition);
                }

                queryBuilder.append(" ").append(logicalOperatorsMap.get(currentLogicalOperator));

                expectedType = ClauseType.FIELD;

            }
        }

        if (currentCondition != null){
            if (currentCondition.field == null)
                throw newParseException("Error parsing method name ", methodName, " - previous condition's field is empty", currentPosition);

            addCondition(queryBuilder, currentCondition, fields, values, valueIndex);
        }

        return queryBuilder.toString();
    }

    private int addCondition(StringBuilder queryBuilder, Condition currentCondition, Map<String, String> fields, Object[] values, int valueIndex) {
        valueIndex++;
        if (valueIndex >= values.length)
            throw new IllegalArgumentException("Error parsing method name - insufficient parameter value for field " + currentCondition.field);

        Object value = values[valueIndex];

        String conditionalOperator = currentCondition.operator;
        if (conditionalOperator == null)
            conditionalOperator = IsEqual;

        if (currentCondition.negative){
            queryBuilder.append(" ").append(logicalOperatorsMap.get(NOT));
        }
        queryBuilder.append(String.format(" %s %s", fields.get(currentCondition.field), conditionalOperatorsMap.get(conditionalOperator)));

        queryBuilder.append(String.format(" '%s'", value.toString()));

        return valueIndex;
    }

    private ParseException newParseException(String head, String mid, String tail, int pos){
        return new ParseException(head + mid + " at position " + pos + tail, head.length() + pos);
    }

}
