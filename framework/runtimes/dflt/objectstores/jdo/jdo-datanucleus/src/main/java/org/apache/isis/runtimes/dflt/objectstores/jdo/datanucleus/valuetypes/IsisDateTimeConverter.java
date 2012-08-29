package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.valuetypes;

import org.apache.isis.applib.value.DateTime;
import org.datanucleus.store.types.converters.TypeConverter;

public class IsisDateTimeConverter implements TypeConverter<DateTime, Long>{

    private static final long serialVersionUID = 1L;

    public IsisDateTimeConverter() {
        
    }
    
    @Override
    public Long toDatastoreType(DateTime memberValue) {
        if(memberValue == null) {
            return null;
        }

        DateTime d = (DateTime)memberValue;
        return d.getMillisSinceEpoch();
    }

    @Override
    public DateTime toMemberType(Long datastoreValue) {
        if(datastoreValue == null) {
            return null;
        }
        return new DateTime(datastoreValue);
    }

}
