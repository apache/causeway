package org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager;

import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;

public class PojoRecreatorDefault implements PojoRecreator, SpecificationLoaderAware {

    private SpecificationLoader specificationLoader;

    public Object recreatePojo(final TypedOid oid) {
        final ObjectSpecification spec = getSpecificationLoader().lookupBySpecId(oid.getObjectSpecId());
        return spec.createObject();
    }

    
    protected SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    @Override
    public void setSpecificationLoader(SpecificationLoader specificationLoader) {
        this.specificationLoader = specificationLoader;
    }

}
