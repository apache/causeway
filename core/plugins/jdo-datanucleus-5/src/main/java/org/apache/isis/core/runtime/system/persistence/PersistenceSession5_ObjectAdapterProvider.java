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

import java.util.function.Function;

import org.datanucleus.enhancement.Persistable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.runtime.system.persistence.adaptermanager.ObjectAdapterContext;

public class PersistenceSession5_ObjectAdapterProvider implements ObjectAdapterProvider {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceSession5_ObjectAdapterProvider.class);

    protected final PersistenceSession5 holder;
    protected final ObjectAdapterContext objectAdapterContext;

    // -- open

    PersistenceSession5_ObjectAdapterProvider(PersistenceSession5 holder, ObjectAdapterContext objectAdapterContext) {
        this.holder = holder;
        this.objectAdapterContext = objectAdapterContext;
    }

    @Override
    public ObjectAdapter adapterFor(Object pojo) {
        //return holder.getObjectAdapterProvider().adapterFor(pojo);
        
        if(pojo == null) {
            return null;
        }
        final ObjectAdapter existingOrValueAdapter = existingOrValueAdapter(pojo);
        if(existingOrValueAdapter != null) {
            return existingOrValueAdapter;
        }

        // Creates a new transient root {@link ObjectAdapter adapter} for the supplied domain
        final RootOid rootOid = holder.createTransientOrViewModelOid(pojo);
        final ObjectAdapter newAdapter = objectAdapterContext.getFactories().createRootAdapter(pojo, rootOid);

        return objectAdapterContext.mapAndInjectServices(newAdapter);
    }

    @Override
    public ObjectAdapter adapterFor(Object pojo, ObjectAdapter parentAdapter, OneToManyAssociation collection) {
        //return holder.getObjectAdapterProvider().adapterFor(pojo, parentAdapter, collection);

        assert parentAdapter != null;
        assert collection != null;

        final ObjectAdapter existingOrValueAdapter = existingOrValueAdapter(pojo);
        if(existingOrValueAdapter != null) {
            return existingOrValueAdapter;
        }

        objectAdapterContext.ensureMapsConsistent(parentAdapter);

        // the List, Set etc. instance gets wrapped in its own adapter
        final ObjectAdapter newAdapter = objectAdapterContext.getFactories()
                .createCollectionAdapter(pojo, parentAdapter, collection);

        return objectAdapterContext.mapAndInjectServices(newAdapter);
    }

    @Override
    public ObjectSpecification specificationForViewModel(Object viewModelPojo) {
        return objectAdapterContext.specificationForViewModel(viewModelPojo);
    }

    @Override
    public ObjectAdapter adapterForViewModel(Object viewModelPojo, Function<ObjectSpecId, RootOid> rootOidFactory) {
        return objectAdapterContext.adapterForViewModel(viewModelPojo, rootOidFactory);
    }
    
    // -- HELPER
    
    protected ObjectAdapter existingOrValueAdapter(Object pojo) {

        // attempt to locate adapter for the pojo
        ObjectAdapter adapter = objectAdapterContext.lookupAdapterByPojo(pojo);
        if (adapter != null) {
            return adapter;
        }

        // pojo may have been lazily loaded by object store, but we haven't yet seen it
        if (pojo instanceof Persistable) {
            adapter = holder.mapPersistent((Persistable) pojo);

            // TODO: could return null if the pojo passed in !dnIsPersistent() || !dnIsDetached()
            // in which case, we would ought to map as a transient object, rather than fall through and treat as a value?
        } else {
            adapter = null;
        }

        if(adapter != null) {
            return adapter;
        }
        
        // need to create (and possibly map) the adapter.
        final ObjectSpecification objSpec = holder.specificationLoader.loadSpecification(pojo.getClass());

        // we create value facets as standalone (so not added to maps)
        if (objSpec.containsFacet(ValueFacet.class)) {
            adapter = objectAdapterContext.getFactories().createStandaloneAdapter(pojo);
            return adapter;
        }

        return null;
    }


}



