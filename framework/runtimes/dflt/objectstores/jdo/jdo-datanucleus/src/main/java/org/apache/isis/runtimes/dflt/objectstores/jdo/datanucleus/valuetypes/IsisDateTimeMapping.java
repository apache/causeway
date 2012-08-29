package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.valuetypes;

import org.datanucleus.store.mapped.mapping.ObjectAsLongMapping;

import org.apache.isis.applib.value.DateTime;

public class IsisDateTimeMapping extends ObjectAsLongMapping {

    private final IsisDateTimeConverter dateConverter = new IsisDateTimeConverter();
    
    public IsisDateTimeMapping() {
        
    }
    
    @Override
    public Class<?> getJavaType() {
        return org.apache.isis.applib.value.DateTime.class;
    }

    @Override
    protected Long objectToLong(Object object) {
        return dateConverter.toDatastoreType((DateTime) object);
    }

    @Override
    protected Object longToObject(Long datastoreValue) {
        return dateConverter.toMemberType(datastoreValue);
    }

}
