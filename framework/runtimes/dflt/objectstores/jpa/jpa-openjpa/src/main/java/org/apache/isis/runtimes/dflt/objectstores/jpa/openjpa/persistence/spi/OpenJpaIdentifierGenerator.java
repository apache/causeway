package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence.spi;

import java.util.UUID;

import javax.persistence.PersistenceUnitUtil;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.map.AdapterMap;
import org.apache.isis.core.metamodel.adapter.map.AdapterMapAware;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.SpecificationLookup;
import org.apache.isis.core.metamodel.spec.SpecificationLookupAware;
import org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.OpenJpaObjectStore;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.IdentifierGenerator;

public class OpenJpaIdentifierGenerator implements IdentifierGenerator, AdapterMapAware, SpecificationLookupAware {

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(OpenJpaIdentifierGenerator.class);
    private AdapterMap adapterMap;
    private SpecificationLookup specificationLookup;


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
        final Object identifier = getPersistenceUnitUtil().getIdentifier(pojo);
        if(identifier == null) {
            // is a service
            return "1";
        }

        // was assuming that the identifier would be an object that we have visibility to
        // but instead, is an internal OpenJPA class (eg IntId)
        // so, the following code doesn't work
//        final ObjectAdapter identifierAdapter = adapterMap.adapterFor(identifier);
//        final EncodableFacet encodableFacet = identifierAdapter.getSpecification().getFacet(EncodableFacet.class);
//        final String identifierAsString = encodableFacet.toEncodedString(identifierAdapter);
//        
//        return identifierAsString;
        
        
        // for now, just using toString().  Suspect will need to review this
        return identifier.toString();
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


    @Override
    public void setSpecificationLookup(SpecificationLookup specificationLookup) {
        this.specificationLookup = specificationLookup;
    }

    
    // //////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // //////////////////////////////////////////////////////////////


    protected PersistenceUnitUtil getPersistenceUnitUtil() {
        final OpenJpaObjectStore objectStore = getObjectStore();
        return objectStore.getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil();
    }


    protected OpenJpaObjectStore getObjectStore() {
        return (OpenJpaObjectStore) IsisContext.getPersistenceSession().getObjectStore();
    }



}
// Copyright (c) Naked Objects Group Ltd.
