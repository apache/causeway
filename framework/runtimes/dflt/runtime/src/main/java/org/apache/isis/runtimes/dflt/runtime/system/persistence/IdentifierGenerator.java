package org.apache.isis.runtimes.dflt.runtime.system.persistence;

import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;

public interface IdentifierGenerator extends DebuggableWithTitle {

    public String createTransientIdentifierFor(ObjectSpecId objectSpecId, final Object pojo);

    public String createAggregateLocalId(ObjectSpecId objectSpecId, final Object pojo, final ObjectAdapter parentAdapter);
    
    public String createPersistentIdentifierFor(ObjectSpecId objectSpecId, Object pojo, RootOid transientRootOid);

}
