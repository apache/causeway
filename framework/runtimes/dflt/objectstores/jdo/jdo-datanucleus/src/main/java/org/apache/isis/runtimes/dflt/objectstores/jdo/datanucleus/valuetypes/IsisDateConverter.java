package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.valuetypes;

import org.apache.isis.applib.value.Date;
import org.datanucleus.store.types.converters.TypeConverter;

public class IsisDateConverter implements TypeConverter<Date, Long>{

    private static final long serialVersionUID = 1L;

    public IsisDateConverter() {
        
    }
    
//    @Override
//    public Long toLong(Date object) {
//        if(object == null) {
//            return null;
//        }
//
//        Date d = (Date)object;
//        return d.getMillisSinceEpoch();
//    }
//
//    @Override
//    public Date toObject(Long value) {
//        if(value == null) {
//            return null;
//        }
//        return new Date(value);
//    }

    @Override
    public Long toDatastoreType(Date memberValue) {
        if(memberValue == null) {
            return null;
        }

        Date d = (Date)memberValue;
        return d.getMillisSinceEpoch();
    }

    @Override
    public Date toMemberType(Long datastoreValue) {
        if(datastoreValue == null) {
            return null;
        }
        return new Date(datastoreValue);
    }

}
