package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.adaptermanager;

import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.DataNucleusObjectStore;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.PojoRecreator;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;

public class DataNucleusPojoRecreator implements PojoRecreator, SpecificationLoaderAware {

    private SpecificationLoader specificationLoader;
    

    @Override
    public Object recreatePojo(TypedOid oid) {
        return getObjectStore().getPojo(oid);
    }

    ///////////////////////////////
    

    protected DataNucleusObjectStore getObjectStore() {
        // REVIEW: inject somehow?
        return (DataNucleusObjectStore) IsisContext.getPersistenceSession().getObjectStore();
    }

    protected SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }
    
    @Override
    public void setSpecificationLoader(SpecificationLoader specificationLoader) {
        this.specificationLoader = specificationLoader;
    };
    
}

