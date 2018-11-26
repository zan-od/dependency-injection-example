package demo.disample.beans;

import org.junit.Test;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SQLQueryBuilderTest {

    @Test(expected = NullPointerException.class)
    public void testBuildUpdateQuery_TableNameIsNull_ShouldThrowException() {
        Map<String, String> fields = new HashMap<>();
        fields.put("field", "value");

        SQLQueryBuilder.buildUpdateQuery(null, fields);
    }

    @Test(expected = NullPointerException.class)
    public void testBuildUpdateQuery_FieldsIsNull_ShouldThrowException() {
        SQLQueryBuilder.buildUpdateQuery("table", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildUpdateQuery_FieldsIsEmpty_ShouldThrowException() {
        Map<String, String> fields = new HashMap<>();

        SQLQueryBuilder.buildUpdateQuery("table", fields);
    }

    @Test
    public void testBuildUpdateQuery_OneField() {
        Map<String, String> fields = new HashMap<>();
        fields.put("field", "value");

        assertEquals(SQLQueryBuilder.buildUpdateQuery("table", fields), "UPDATE table SET field = 'value'");
    }

    @Test
    public void testBuildUpdateQuery_TwoField() {
        Map<String, String> fields = new HashMap<>();
        fields.put("field1", "value1");
        fields.put("field2", "value2");

        assertEquals(SQLQueryBuilder.buildUpdateQuery("table", fields), "UPDATE table SET field1 = 'value1', field2 = 'value2'");
    }

    @Test(expected = NullPointerException.class)
    public void testBuildInsertQuery_TableNameIsNull_ShouldThrowException() {
        Map<String, String> fields = new HashMap<>();
        fields.put("field", "value");

        SQLQueryBuilder.buildInsertQuery(null, fields);
    }

    @Test(expected = NullPointerException.class)
    public void testBuildInsertQuery_FieldsIsNull_ShouldThrowException() {
        SQLQueryBuilder.buildInsertQuery("table", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildInsertQuery_FieldsIsEmpty_ShouldThrowException() {
        Map<String, String> fields = new HashMap<>();

        SQLQueryBuilder.buildInsertQuery("table", fields);
    }

    @Test
    public void testBuildInsertQuery_OneField() {
        Map<String, String> fields = new HashMap<>();
        fields.put("field", "value");

        assertEquals(SQLQueryBuilder.buildInsertQuery("table", fields), "INSERT INTO table (field) VALUES ('value')");
    }

    @Test
    public void testBuildInsertQuery_TwoField() {
        Map<String, String> fields = new HashMap<>();
        fields.put("field1", "value1");
        fields.put("field2", "value2");

        assertEquals(SQLQueryBuilder.buildInsertQuery("table", fields), "INSERT INTO table (field1, field2) VALUES ('value1', 'value2')");
    }

    @Test(expected = NullPointerException.class)
    public void testParseQuery_MethodNameIsNull_ShouldThrowException() throws ParseException {
        Map<String, String> fields = new HashMap<>();
        Object[] values = {};

        SQLQueryBuilder builder = new SQLQueryBuilder();
        builder.parseQuery(null, fields, values);
    }

    @Test(expected = NullPointerException.class)
    public void testParseQuery_FieldsIsNull_ShouldThrowException() throws ParseException {
        Object[] values = {};

        SQLQueryBuilder builder = new SQLQueryBuilder();
        builder.parseQuery("method", null, values);
    }

    @Test(expected = NullPointerException.class)
    public void testParseQuery_ValuesIsNull_ShouldThrowException() throws ParseException {
        Map<String, String> fields = new HashMap<>();

        SQLQueryBuilder builder = new SQLQueryBuilder();
        builder.parseQuery("method", fields, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseQuery_IllegalMethodName_ShouldThrowException() throws ParseException {
        Map<String, String> fields = new HashMap<>();
        Object[] values = {};

        SQLQueryBuilder builder = new SQLQueryBuilder();
        builder.parseQuery("searchBy", fields, values);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseQuery_NothingToParse_ShouldThrowException() throws ParseException {
        Map<String, String> fields = new HashMap<>();
        Object[] values = {};

        SQLQueryBuilder builder = new SQLQueryBuilder();
        builder.parseQuery("findBy", fields, values);
    }

    @Test(expected = ParseException.class)
    public void testParseQuery_UnknownField_ShouldThrowException() throws ParseException {
        Map<String, String> fields = new HashMap<>();
        Object[] values = {};

        SQLQueryBuilder builder = new SQLQueryBuilder();
        builder.parseQuery("findByUnknownField", fields, values);
    }

    @Test
    public void testParseQuery_KnownField() throws ParseException {
        Map<String, String> fields = new HashMap<>();
        fields.put("knownField", "KNOWN_FIELD");
        Object[] values = {"value"};

        SQLQueryBuilder builder = new SQLQueryBuilder();
        assertEquals(builder.parseQuery("findByKnownField", fields, values), " KNOWN_FIELD = 'value'");
    }

    @Test(expected = ParseException.class)
    public void testParseQuery_NotCapitalizedFieldName_ShouldThrowException() throws ParseException {
        Map<String, String> fields = new HashMap<>();
        fields.put("knownField", "KNOWN_FIELD");
        Object[] values = {"value"};

        SQLQueryBuilder builder = new SQLQueryBuilder();
        builder.parseQuery("findByknownField", fields, values);
    }

    @Test(expected = ParseException.class)
    public void testParseQuery_TwoFieldsNear_ShouldThrowException() throws ParseException {
        Map<String, String> fields = new HashMap<>();
        fields.put("field1", "FIELD1");
        fields.put("field2", "FIELD2");
        Object[] values = {1, 2};

        SQLQueryBuilder builder = new SQLQueryBuilder();
        builder.parseQuery("findByField1Field2", fields, values);
    }

    @Test
    public void testParseQuery_NegativeCondition() throws ParseException {
        Map<String, String> fields = new HashMap<>();
        fields.put("field1", "FIELD1");
        fields.put("field2", "FIELD2");
        Object[] values = {1, 2};

        SQLQueryBuilder builder = new SQLQueryBuilder();
        assertEquals(builder.parseQuery("findByField1NotLess", fields, values), " NOT FIELD1 < '1'");
    }

    @Test(expected = ParseException.class)
    public void testParseQuery_TwoLogicalOperatorsNear_ShouldThrowException() throws ParseException {
        Map<String, String> fields = new HashMap<>();
        fields.put("field1", "FIELD1");
        fields.put("field2", "FIELD2");
        Object[] values = {1, 2};

        SQLQueryBuilder builder = new SQLQueryBuilder();
        builder.parseQuery("findByField1AndOrField2", fields, values);
    }

    @Test
    public void testParseQuery_AndOperator() throws ParseException {
        Map<String, String> fields = new HashMap<>();
        fields.put("field1", "FIELD1");
        fields.put("field2", "FIELD2");
        Object[] values = {1, 2};

        SQLQueryBuilder builder = new SQLQueryBuilder();
        assertEquals(builder.parseQuery("findByField1AndField2", fields, values), " FIELD1 = '1' AND FIELD2 = '2'");
    }

    @Test
    public void testParseQuery_OrOperator() throws ParseException {
        Map<String, String> fields = new HashMap<>();
        fields.put("field1", "FIELD1");
        fields.put("field2", "FIELD2");
        Object[] values = {1, 2};

        SQLQueryBuilder builder = new SQLQueryBuilder();
        assertEquals(builder.parseQuery("findByField1OrField2", fields, values), " FIELD1 = '1' OR FIELD2 = '2'");
    }

    @Test(expected = ParseException.class)
    public void testParseQuery_LogicalOperatorAtBegin_ShouldThrowException() throws ParseException {
        Map<String, String> fields = new HashMap<>();
        fields.put("field1", "FIELD1");
        fields.put("field2", "FIELD2");
        Object[] values = {1, 2};

        SQLQueryBuilder builder = new SQLQueryBuilder();
        builder.parseQuery("findByAndField1OrField2", fields, values);
    }

    @Test(expected = ParseException.class)
    public void testParseQuery_LogicalOperatorAtEnd_ShouldThrowException() throws ParseException {
        Map<String, String> fields = new HashMap<>();
        fields.put("field1", "FIELD1");
        fields.put("field2", "FIELD2");
        Object[] values = {1, 2};

        SQLQueryBuilder builder = new SQLQueryBuilder();
        builder.parseQuery("findByField1OrField2And", fields, values);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseQuery_ValuesCountLessThanNeeded_ShouldThrowException() throws ParseException {
        Map<String, String> fields = new HashMap<>();
        fields.put("field1", "FIELD1");
        fields.put("field2", "FIELD2");
        Object[] values = {1};

        SQLQueryBuilder builder = new SQLQueryBuilder();
        builder.parseQuery("findByField1OrField2", fields, values);
    }
}