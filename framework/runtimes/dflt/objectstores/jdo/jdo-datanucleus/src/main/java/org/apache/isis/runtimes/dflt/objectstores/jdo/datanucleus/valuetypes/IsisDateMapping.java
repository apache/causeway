package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.valuetypes;

import org.datanucleus.store.mapped.mapping.ObjectAsLongMapping;

import org.apache.isis.applib.value.Date;

public class IsisDateMapping extends ObjectAsLongMapping {

    private final IsisDateConverter dateConverter = new IsisDateConverter();
    
    public IsisDateMapping() {
        
    }
    
    @Override
    public Class<?> getJavaType() {
        return org.apache.isis.applib.value.Date.class;
    }

    @Override
    protected Long objectToLong(Object object) {
        return dateConverter.toDatastoreType((Date) object);
    }

    @Override
    protected Object longToObject(Long datastoreValue) {
        return dateConverter.toMemberType(datastoreValue);
    }

}
