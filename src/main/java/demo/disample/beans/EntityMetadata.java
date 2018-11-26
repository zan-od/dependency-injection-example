package demo.disample.beans;

import java.text.ParseException;
import java.util.List;

interface EntityMetadata<ENTITY> {

    void setGenericTypes(Class[] genericTypes) throws Exception;

    List<ENTITY> findByConditions(String methodName, Object[] values) throws ParseException;

}
