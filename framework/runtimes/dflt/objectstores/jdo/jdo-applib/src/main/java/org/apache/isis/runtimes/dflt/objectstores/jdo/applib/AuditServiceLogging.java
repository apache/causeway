package org.apache.isis.runtimes.dflt.objectstores.jdo.applib;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.log4j.Logger;

public class AuditServiceLogging extends AbstractFactoryAndRepository  implements AuditService {
    
    private static Logger LOG = Logger.getLogger(AuditServiceLogging.class);
    
    @Hidden
    public void audit(String user, long currentTimestampEpoch, String objectType, String identifier, String preValue, String postValue) {
        String auditMessage = objectType + ":" + identifier + " by " + user + ": " + preValue + " -> " + postValue; 
        LOG.info(auditMessage);
    }
    
}
