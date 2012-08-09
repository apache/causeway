package org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager;

import org.apache.isis.core.metamodel.adapter.oid.TypedOid;

public interface PojoRecreator {

    Object recreatePojo(final TypedOid oid);
    
}
