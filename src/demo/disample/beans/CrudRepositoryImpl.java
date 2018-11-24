package demo.disample.beans;

import demo.disample.annotations.Column;
import demo.disample.annotations.Component;
import demo.disample.annotations.Id;
import demo.disample.annotations.Table;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
            if (!entityClass.isAnnotationPresent(Table.class)) {
                throw new Exception("Error resolving table name. Entity type " + entityClass.getName() + " don't have @Table annotation");
            }

            tableName = ((Table) entityClass.getAnnotation(Table.class)).name();
        }

        private void resolveFields() throws Exception {
            int idsCount = 0;
            for (Field field : entityClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Column.class)) {
                    String columnName = field.getAnnotation(Column.class).name();

                    fieldsToColumns.put(field.getName(), columnName);
                    columnsToFields.put(columnName, field.getName());

                    if (field.isAnnotationPresent(Id.class)){
                        idFieldName = field.getName();
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

        String getIdColumnName(){
            return fieldsToColumns.get(idFieldName);
        }
    }

    private Metadata loadMetadata(Class idClass, Class entityClass) throws Exception {
        Metadata metadata = new Metadata();
        metadata.idClass = idClass;
        metadata.entityClass = entityClass;
        metadata.resolveTableName();
        metadata.resolveFields();

        return metadata;
    }

    private String buildUpdateQuery(Map<String, String> fields){
        if (fields == null)
            throw new IllegalArgumentException("Field set is null");

        if (fields.size() == 0)
            throw new IllegalArgumentException("Field set is empty");

        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        boolean first = true;
        for (Map.Entry entry: fields.entrySet()) {
            if (!first){
                columns.append(",");
                values.append(",");
            }
            first = false;

            columns.append(entry.getKey());
            values.append("'").append(entry.getValue()).append("'");
        }

        return "UPDATE " + metadata.tableName + " SET (" + columns.toString() + ") VALUES (" + values.toString() + ")";
    }

    private String capitalizeWord(String word){
        if (word == null)
            return null;

        if (word.isEmpty())
            return word;

        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    private Map<String, String> getEntityFieldValues(ENTITY obj) throws Exception {
        Map<String, String> fields = new HashMap<>();

        for (Map.Entry entry: metadata.fieldsToColumns.entrySet()){
            String fieldName = (String) entry.getKey();
            Method method;
            try {
                method = metadata.entityClass.getMethod("get" + capitalizeWord(fieldName));
            } catch (NoSuchMethodException e) {
                throw new Exception("Error getting field value of " + metadata.entityClass.getName() + ". Get method of field '" + fieldName + "' not found.");
            }

            String value = method.invoke(obj).toString();

            fields.put(fieldName, value);
        }

        return fields;
    }
    
    @Override
    public ENTITY save(ENTITY obj) throws Exception {
        //System.out.println("Saving object {" + obj.toString() + "}");

        String query = buildUpdateQuery(getEntityFieldValues(obj));
        System.out.println(query);

        return obj;
    }

    @Override
    public ENTITY getOne(ID id){
        System.out.println("SELECT * FROM " + metadata.tableName + " WHERE " + metadata.getIdColumnName() + " = " + id);

        try {
            return (ENTITY) metadata.entityClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void delete(ID id) {
        System.out.println("DELETE FROM " + metadata.tableName + " WHERE " + metadata.getIdColumnName() + " = " + id);
    }

    @Override
    public List<ENTITY> listAll() {
        System.out.println("SELECT * FROM " + metadata.tableName);
        return new ArrayList<>();
    }

    @Override
    public List<ENTITY> findByConditions(String methodName, Object[] values) {
        return null;
    }

}
