package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.spi;

import java.util.UUID;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.map.AdapterMap;
import org.apache.isis.core.metamodel.adapter.map.AdapterMapAware;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.extensions.jpa.metamodel.util.JpaPropertyUtils;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.IdentifierGenerator;

public class OpenJpaIdentifierGenerator implements IdentifierGenerator, AdapterMapAware {

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(OpenJpaIdentifierGenerator.class);
    private AdapterMap adapterMap;


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
        final ObjectAdapter adapter = adapterMap.adapterFor(pojo);
        final OneToOneAssociation idPropertyFor = JpaPropertyUtils.getIdPropertyFor(adapter.getSpecification());
        if (idPropertyFor == null) {
            throw new IllegalStateException("cannot find id property for pojo");
        }
        final PropertyOrCollectionAccessorFacet facet = idPropertyFor.getFacet(PropertyOrCollectionAccessorFacet.class);
        final Object propertyValue = facet.getProperty(adapter);
        return propertyValue.toString();
    }


    // //////////////////////////////////////////////////////////////
    // Debugging
    // //////////////////////////////////////////////////////////////


    public String debugTitle() {
        return "OpenJpa Identifier Generator";
    }

    
    @Override
    public void debugData(DebugBuilder debug) {
        
    }


    // //////////////////////////////////////////////////////////////
    // Dependencies (injected by setter)
    // //////////////////////////////////////////////////////////////

    @Override
    public void setAdapterMap(AdapterMap adapterMap) {
        this.adapterMap = adapterMap;
    }


}
// Copyright (c) Naked Objects Group Ltd.
