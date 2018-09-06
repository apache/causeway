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
package org.apache.isis.core.runtime.system.persistence.adaptermanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

/**
 * package private mixin for ObjectAdapterContext
 * <p>
 * Responsibility: creates RootOids 
 * </p> 
 * @since 2.0.0-M2
 */
@SuppressWarnings("unused")
class ObjectAdapterContext_NewIdentifier {
    
    
    private static final Logger LOG = LoggerFactory.getLogger(ObjectAdapterContext_NewIdentifier.class);
    private final ObjectAdapterContext objectAdapterContext;
    private final PersistenceSession persistenceSession;
    private final ServicesInjector servicesInjector;
    private final SpecificationLoader specificationLoader;
    
    
    ObjectAdapterContext_NewIdentifier(ObjectAdapterContext objectAdapterContext,
            PersistenceSession persistenceSession) {
        this.objectAdapterContext = objectAdapterContext;
        this.persistenceSession = persistenceSession;
        this.servicesInjector = persistenceSession.getServicesInjector();
        this.specificationLoader = servicesInjector.getSpecificationLoader();
    }
    
//    RootOid rootOidFor(Object pojo) {
//        final RootOid rootOid = servicesInjector.isRegisteredServiceInstance(pojo) 
//                ? createPersistentOrViewModelOid(pojo) 
//                        : createTransientOrViewModelOid(pojo);
//        
//                
//        return rootOid;
//    }
    
    
    // -- create...Oid (main API)
    /**
     * Create a new {@link Oid#isTransient() transient} {@link Oid} for the
     * supplied pojo, uniquely distinguishable from any other {@link Oid}.
     */
    final RootOid createTransientOrViewModelOid(final Object pojo) {
        return newIdentifier(pojo, Oid.State.TRANSIENT);
    }

    /**
     * Return an equivalent {@link RootOid}, but being persistent.
     *
     * <p>
     * It is the responsibility of the implementation to determine the new unique identifier.
     * For example, the generator may simply assign a new value from a sequence, or a GUID;
     * or, the generator may use the oid to look up the object and inspect the object in order
     * to obtain an application-defined value.
     *
     * @param pojo - being persisted
     */
    final RootOid createPersistentOrViewModelOid(Object pojo) {
        return newIdentifier(pojo, Oid.State.PERSISTENT);
    }

    RootOid newIdentifier(final Object pojo, final Oid.State type) {
        final ObjectSpecification spec = objectSpecFor(pojo);
        if(spec.isService()) {
            return newRootId(spec, PersistenceSession.SERVICE_IDENTIFIER, Oid.State.PERSISTENT); // services are always persistent
        }

        final ViewModelFacet recreatableObjectFacet = spec.getFacet(ViewModelFacet.class);
        final String identifier =
                recreatableObjectFacet != null
                ? recreatableObjectFacet.memento(pojo)
                        : persistenceSession.identifierFor(pojo, type);

                return newRootId(spec, identifier, type);
    }
    
    
    private RootOid newRootId(final ObjectSpecification spec, final String identifier, final Oid.State type) {
        final Oid.State state =
                spec.containsDoOpFacet(ViewModelFacet.class)
                ? Oid.State.VIEWMODEL
                        : type == Oid.State.TRANSIENT
                        ? Oid.State.TRANSIENT
                                : Oid.State.PERSISTENT;
        final ObjectSpecId objectSpecId = spec.getSpecId();
        return new RootOid(objectSpecId, identifier, state);
    }
    
    // -- HELPER
    
    private ObjectSpecification objectSpecFor(final Object pojo) {
        final Class<?> pojoClass = pojo.getClass();
        return specificationLoader.loadSpecification(pojoClass);
    }


    
}