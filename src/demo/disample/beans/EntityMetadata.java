package demo.disample.beans;

import java.util.List;

interface EntityMetadata<ENTITY> {

    void setGenericTypes(Class[] genericTypes) throws Exception;

    List<ENTITY> findByConditions(String methodName, Object[] values);

}
