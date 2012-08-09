package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.spi;

import java.util.UUID;

import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.DataNucleusObjectStore;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.IdentifierGenerator;

public class DataNucleusIdentifierGenerator implements IdentifierGenerator {

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(DataNucleusIdentifierGenerator.class);
    


    // //////////////////////////////////////////////////////////////
    // main api
    // //////////////////////////////////////////////////////////////

    @Override
    public String createTransientIdentifierFor(ObjectSpecId objectSpecId, Object pojo) {
        return UUID.randomUUID().toString();
    }


    @Override
    public String createAggregateLocalId(ObjectSpecId objectSpecId, Object pojo, ObjectAdapter parentAdapter) {
        return UUID.randomUUID().toString();
    }


    @Override
    public String createPersistentIdentifierFor(ObjectSpecId objectSpecId, Object pojo, RootOid transientRootOid) {
        
        // hack to deal with services
        if(!(pojo instanceof PersistenceCapable)) {
            return "1";
        }
        
        final Object jdoOid = getJdoPersistenceManager().getObjectId(pojo);
        
        return JdoOidSerializer.toString(jdoOid);
    }



    // //////////////////////////////////////////////////////////////
    // Debugging
    // //////////////////////////////////////////////////////////////


    public String debugTitle() {
        return "DataNucleus Identifier Generator";
    }

    
    @Override
    public void debugData(DebugBuilder debug) {
        
    }

    
    // //////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // //////////////////////////////////////////////////////////////


    protected PersistenceManager getJdoPersistenceManager() {
        final DataNucleusObjectStore objectStore = getDataNucleusObjectStore();
        return objectStore.getPersistenceManager();
    }


    protected DataNucleusObjectStore getDataNucleusObjectStore() {
        return (DataNucleusObjectStore) IsisContext.getPersistenceSession().getObjectStore();
    }

}
// Copyright (c) Naked Objects Group Ltd.
