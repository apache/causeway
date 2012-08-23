package org.apache.isis.runtimes.dflt.objectstores.jdo.applib;

import org.apache.isis.applib.annotation.Hidden;

public interface AuditService {
    
    @Hidden
    public void audit(String user, long currentTimestampEpoch, String objectType, String identifier, String preValue, String postValue);
    
}
