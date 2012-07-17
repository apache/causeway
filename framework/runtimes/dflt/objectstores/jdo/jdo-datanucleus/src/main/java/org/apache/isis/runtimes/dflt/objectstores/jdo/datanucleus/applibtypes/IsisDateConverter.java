package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.applibtypes;

import org.datanucleus.store.types.ObjectLongConverter;

import org.apache.isis.applib.value.Date;

public class IsisDateConverter implements ObjectLongConverter<Date>{

    private static final long serialVersionUID = 1L;

    public IsisDateConverter() {
        
    }
    
    @Override
    public Long toLong(Date object) {
        if(object == null) {
            return null;
        }

        Date d = (Date)object;
        return d.getMillisSinceEpoch();
    }

    @Override
    public Date toObject(Long value) {
        if(value == null) {
            return null;
        }
        return new Date(value);
    }

}
