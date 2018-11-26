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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class CrudRepositoryImpl<ID, ENTITY> implements CrudRepository<ID, ENTITY>, EntityMetadata<ENTITY> {

    private final AtomicReference<Metadata> metadataReference;

    public CrudRepositoryImpl(){
        this.metadataReference = new AtomicReference<>();
    }

    @Override
    public void setGenericTypes(Class[] genericTypes) throws Exception {
        if (genericTypes == null)
            throw new IllegalArgumentException("Generic types array is null");

        if (genericTypes.length != 2)
            throw new IllegalArgumentException("Generic types array must contain two types");

        Metadata.Builder builder = new Metadata.Builder();
        this.metadataReference.set(
                builder
                .setIdClass(genericTypes[0])
                .setEntityClass(genericTypes[1])
                .build());
    }

    private static final class Metadata {
        private final Class idClass;
        private final Class entityClass;
        private final String tableName;
        private final String idFieldName;
        private final Map<String, String> fieldsToColumns;
        private final Map<String, String> columnsToFields;

        private Metadata(Builder builder){
            this.idClass = builder.idClass;
            this.entityClass = builder.entityClass;
            this.tableName = builder.tableName;
            this.idFieldName = builder.idFieldName;

            this.fieldsToColumns = builder.fieldsToColumns;
            this.columnsToFields = builder.columnsToFields;
        }

        private static class Builder{
            private Class idClass;
            private Class entityClass;
            private String tableName;
            private String idFieldName;
            private final Map<String, String> fieldsToColumns = new ConcurrentHashMap<>();
            private final Map<String, String> columnsToFields = new ConcurrentHashMap<>();

            public Builder setIdClass(Class idClass){
                this.idClass = idClass;
                return this;
            }

            public Builder setEntityClass(Class entityClass){
                this.entityClass = entityClass;
                return this;
            }

            private void resolveTableName() throws Exception {
                if (!this.entityClass.isAnnotationPresent(Table.class)) {
                    throw new Exception("Error resolving table name. Entity type " + this.entityClass.getName() + " don't have @Table annotation");
                }

                this.tableName =((Table) this.entityClass.getAnnotation(Table.class)).name();
            }

            private void resolveFields() throws Exception {
                int idsCount = 0;
                for (Field field : this.entityClass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Column.class)) {
                        String columnName = field.getAnnotation(Column.class).name();

                        fieldsToColumns.put(field.getName(), columnName);
                        columnsToFields.put(columnName, field.getName());

                        if (field.isAnnotationPresent(Id.class)){
                            this.idFieldName = field.getName();
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

            public Metadata build() throws Exception {
                resolveTableName();
                resolveFields();

                return new Metadata(this);
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

        public Class getEntityClass() {
            return entityClass;
        }

        public String getTableName() {
            return tableName;
        }

        public String getIdFieldName() {
            return idFieldName;
        }

        public Map<String, String> getFieldsToColumns() {
            return fieldsToColumns;
        }

        public Map<String, String> getColumnsToFields() {
            return columnsToFields;
        }
    }

    private Metadata getMetadata() {
        return metadataReference.get();
    }

    private Map<String, Object> getEntityFieldValues(ENTITY obj) throws Exception {
        Map<String, Object> fields = new HashMap<>();

        for (Map.Entry entry: getMetadata().getFieldsToColumns().entrySet()){
            String fieldName = (String) entry.getKey();
            Method method;
            try {
                method = getMetadata().getEntityClass().getMethod("get" + Utils.capitalizeWord(fieldName));
            } catch (NoSuchMethodException e) {
                throw new Exception("Error getting field value of " + getMetadata().getEntityClass().getName() + ". Get method of field '" + fieldName + "' not found.");
            }

            Object value = method.invoke(obj);

            fields.put(fieldName, value);
        }

        return fields;
    }

    private Map<String, String> getColumnsAndValues(Map<String, Object> fieldValues){
        Map<String, String> columnsAndValues = new HashMap<>();
        for (Map.Entry entry: fieldValues.entrySet()){
            columnsAndValues.put(getMetadata().getColumnName(entry.getKey().toString()), entry.getValue().toString());
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

        return String.format(" WHERE %s = '%s'", getMetadata().getIdColumnName(), id);
    }

    @Override
    public ENTITY save(ENTITY obj) throws Exception {
        //System.out.println("Saving object {" + obj.toString() + "}");

        Map<String, String> values = getColumnsAndValues(getEntityFieldValues(obj));
        String id = values.get(getMetadata().getIdColumnName());

        String query;
        if ((id == null) || (id.isEmpty()))
            query = SQLQueryBuilder.buildInsertQuery(getMetadata().getTableName(), values);
        else
            query = SQLQueryBuilder.buildUpdateQuery(getMetadata().getTableName(), values) + getQueryFilterById(id);

        System.out.println(query);

        return obj;
    }

    @Override
    public ENTITY getOne(ID id){
        System.out.println("SELECT * FROM " + getMetadata().getTableName() + getQueryFilterById(id));

        try {
            return (ENTITY) getMetadata().getEntityClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void delete(ID id) {
        System.out.println("DELETE FROM " + getMetadata().getTableName() + getQueryFilterById(id));
    }

    @Override
    public List<ENTITY> listAll() {
        System.out.println("SELECT * FROM " + getMetadata().getTableName());
        return new ArrayList<>();
    }

    @Override
    public List<ENTITY> findByConditions(String methodName, Object[] values) throws ParseException {
        SQLQueryBuilder builder = new SQLQueryBuilder();

        String query = "SELECT * FROM "  + getMetadata().getTableName() + " WHERE" + builder.parseQuery(methodName, getMetadata().fieldsToColumns , values);
        System.out.println(query);
        return new ArrayList<>();
    }

}
