package demo.disample.beans;

import demo.disample.annotations.Column;
import demo.disample.annotations.Component;
import demo.disample.annotations.Id;
import demo.disample.annotations.Table;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CrudRepositoryImpl<ID, ENTITY> implements CrudRepository<ID, ENTITY>, EntityMetadata<ENTITY> {

    private Metadata metadata;

    @Override
    public void setGenericTypes(Class[] genericTypes) throws Exception {
        if (genericTypes == null)
            throw new IllegalArgumentException("Generic types array is null");

        if (genericTypes.length != 2)
            throw new IllegalArgumentException("Generic types array must contain two types");



        this.metadata = loadMetadata(genericTypes[0], genericTypes[1]);
    }

    private class Metadata {
        private Class idClass;
        private Class entityClass;
        private String tableName;
        private String idFieldName;
        private final Map<String, String> fieldsToColumns = new HashMap<>();
        private Map<String, String> columnsToFields = new HashMap<>();

        private void resolveTableName() throws Exception {
            if (!getEntityClass().isAnnotationPresent(Table.class)) {
                throw new Exception("Error resolving table name. Entity type " + getEntityClass().getName() + " don't have @Table annotation");
            }

            setTableName(((Table) getEntityClass().getAnnotation(Table.class)).name());
        }

        private void resolveFields() throws Exception {
            int idsCount = 0;
            for (Field field : getEntityClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Column.class)) {
                    String columnName = field.getAnnotation(Column.class).name();

                    getFieldsToColumns().put(field.getName(), columnName);
                    getColumnsToFields().put(columnName, field.getName());

                    if (field.isAnnotationPresent(Id.class)){
                        setIdFieldName(field.getName());
                        idsCount++;
                    }
                }
            }

            if (idsCount == 0){
                throw new Exception("No @Id fields found");
            } else if (idsCount > 1) {
                throw new Exception("More than one @Id field found");
            }
        }

        String getColumnName(String fieldName){
            return getFieldsToColumns().get(fieldName);
        }

        String getIdColumnName(){
            return getColumnName(getIdFieldName());
        }

        public Class getIdClass() {
            return idClass;
        }

        private void setIdClass(Class idClass) {
            this.idClass = idClass;
        }

        public Class getEntityClass() {
            return entityClass;
        }

        private void setEntityClass(Class entityClass) {
            this.entityClass = entityClass;
        }

        public String getTableName() {
            return tableName;
        }

        private void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getIdFieldName() {
            return idFieldName;
        }

        private void setIdFieldName(String idFieldName) {
            this.idFieldName = idFieldName;
        }

        public Map<String, String> getFieldsToColumns() {
            return fieldsToColumns;
        }

        public Map<String, String> getColumnsToFields() {
            return columnsToFields;
        }

        private void setColumnsToFields(Map<String, String> columnsToFields) {
            this.columnsToFields = columnsToFields;
        }
    }

    private Metadata loadMetadata(Class idClass, Class entityClass) throws Exception {
        Metadata metadata = new Metadata();
        metadata.setIdClass(idClass);
        metadata.setEntityClass(entityClass);
        metadata.resolveTableName();
        metadata.resolveFields();

        return metadata;
    }

    private Map<String, Object> getEntityFieldValues(ENTITY obj) throws Exception {
        Map<String, Object> fields = new HashMap<>();

        for (Map.Entry entry: metadata.getFieldsToColumns().entrySet()){
            String fieldName = (String) entry.getKey();
            Method method;
            try {
                method = metadata.getEntityClass().getMethod("get" + Utils.capitalizeWord(fieldName));
            } catch (NoSuchMethodException e) {
                throw new Exception("Error getting field value of " + metadata.getEntityClass().getName() + ". Get method of field '" + fieldName + "' not found.");
            }

            Object value = method.invoke(obj);

            fields.put(fieldName, value);
        }

        return fields;
    }

    private Map<String, String> getColumnsAndValues(Map<String, Object> fieldValues){
        Map<String, String> columnsAndValues = new HashMap<>();
        for (Map.Entry entry: fieldValues.entrySet()){
            columnsAndValues.put(metadata.getColumnName(entry.getKey().toString()), entry.getValue().toString());
        }

        return columnsAndValues;
    }

    private String getQueryFilterById(ID id){
        if (id == null){
            throw new NullPointerException("Id value must not be null");
        }

        return getQueryFilterById(id.toString());
    }

    private String getQueryFilterById(String id){
        if (id == null){
            throw new NullPointerException("Id value must not be null");
        }

        return String.format(" WHERE %s = '%s'", metadata.getIdColumnName(), id);
    }

    @Override
    public ENTITY save(ENTITY obj) throws Exception {
        //System.out.println("Saving object {" + obj.toString() + "}");

        Map<String, String> values = getColumnsAndValues(getEntityFieldValues(obj));
        String id = values.get(metadata.getIdColumnName());

        String query;
        if ((id == null) || (id.isEmpty()))
            query = SQLQueryBuilder.buildInsertQuery(metadata.getTableName(), values);
        else
            query = SQLQueryBuilder.buildUpdateQuery(metadata.getTableName(), values) + getQueryFilterById(id);

        System.out.println(query);

        return obj;
    }

    @Override
    public ENTITY getOne(ID id){
        System.out.println("SELECT * FROM " + metadata.getTableName() + getQueryFilterById(id));

        try {
            return (ENTITY) metadata.getEntityClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void delete(ID id) {
        System.out.println("DELETE FROM " + metadata.getTableName() + getQueryFilterById(id));
    }

    @Override
    public List<ENTITY> listAll() {
        System.out.println("SELECT * FROM " + metadata.getTableName());
        return new ArrayList<>();
    }

    @Override
    public List<ENTITY> findByConditions(String methodName, Object[] values) throws ParseException {
        SQLQueryBuilder builder = new SQLQueryBuilder();

        String query = "SELECT * FROM "  + metadata.getTableName() + " WHERE" + builder.parseQuery(methodName, metadata.fieldsToColumns , values);
        System.out.println(query);
        return new ArrayList<>();
    }

}
