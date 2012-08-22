package org.apache.isis.runtimes.dflt.objectstores.jdo.applib;

import java.util.List;

public class AuditServiceDemo extends AuditServiceAbstract {
    
    public List<AuditEntry> list() {
        return allInstances(AuditEntry.class);
    }
    
}
