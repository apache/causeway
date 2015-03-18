/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.core.runtime.system.persistence;

import java.util.UUID;

import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.objectstore.jdo.datanucleus.DataNucleusObjectStore;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.spi.JdoObjectIdSerializer;

class IdentifierGeneratorForDataNucleus implements IdentifierGenerator {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(IdentifierGeneratorForDataNucleus.class);
    


    // //////////////////////////////////////////////////////////////
    // main api
    // //////////////////////////////////////////////////////////////

    /**
     * TODO: this is really to create a transient or view model identifier.  The responsibilities are split unhappily between this class and its caller, the OidGenerator.
     */
    @Override
    public String createTransientIdentifierFor(ObjectSpecId objectSpecId, Object pojo) {

        final ObjectSpecification spec = getSpecificationLoader().lookupBySpecId(objectSpecId);
        final ViewModelFacet recreatableObjectFacet = spec.getFacet(ViewModelFacet.class);
        if(recreatableObjectFacet != null) {
            return recreatableObjectFacet.memento(pojo);
        }

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
        
        final ObjectSpecification spec = getSpecificationLookup().lookupBySpecId(objectSpecId);
        if(spec.containsFacet(ViewModelFacet.class)) {
            ViewModelFacet recreatableObjectFacet = spec.getFacet(ViewModelFacet.class);
            return recreatableObjectFacet.memento(pojo);
        }
        final Object jdoOid = getJdoPersistenceManager().getObjectId(pojo);

        return JdoObjectIdSerializer.toOidIdentifier(jdoOid);
    }



    @Override
    public <T extends IdentifierGenerator> T underlying(Class<T> cls) {
        return cls == IdentifierGeneratorForDataNucleus.class? (T) this : null;
    }

    //////////////////////////////////////////////////////////////////
    // context
    //////////////////////////////////////////////////////////////////

    protected SpecificationLoader getSpecificationLookup() {
        return IsisContext.getSpecificationLoader();
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
    protected IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }
    protected SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

}
// Copyright (c) Naked Objects Group Ltd.
