package org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager;

import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;

public class PojoRecreatorDefault implements PojoRecreator {

    public Object recreatePojo(final TypedOid oid) {
        final ObjectSpecification spec = getSpecificationLoader().lookupBySpecId(oid.getObjectSpecId());
        return spec.createObject();
    }

    
    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

}
