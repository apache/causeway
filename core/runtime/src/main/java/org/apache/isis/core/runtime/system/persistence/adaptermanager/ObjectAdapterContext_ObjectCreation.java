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

import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.events.lifecycle.AbstractLifecycleEvent;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.CreatedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.CreatedLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.LifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

/**
 * package private mixin for ObjectAdapterContext
 * <p>
 * Responsibility: re-creates domain object instances  
 * </p> 
 * @since 2.0.0-M2
 */
@SuppressWarnings("unused")
class ObjectAdapterContext_ObjectCreation {
    
    
    private static final Logger LOG = LoggerFactory.getLogger(ObjectAdapterContext_ObjectCreation.class);
    private final ObjectAdapterContext objectAdapterContext;
    private final PersistenceSession persistenceSession;
    private final ServicesInjector servicesInjector;
    private final SpecificationLoader specificationLoader;
    
    ObjectAdapterContext_ObjectCreation(ObjectAdapterContext objectAdapterContext,
            PersistenceSession persistenceSession) {
        this.objectAdapterContext = objectAdapterContext;
        this.persistenceSession = persistenceSession;
        this.servicesInjector = persistenceSession.getServicesInjector();
        this.specificationLoader = servicesInjector.getSpecificationLoader();
    }
    
    public ObjectAdapter newInstance(ObjectSpecification objectSpec) {
        return newInstance(objectSpec, Variant.TRANSIENT, null);
    }

    public ObjectAdapter recreateInstance(ObjectSpecification objectSpec, @Nullable String memento) {
        return newInstance(objectSpec, Variant.VIEW_MODEL, memento);
    }
    
    //  -- HELPER
    
    private enum Variant {
        TRANSIENT,
        VIEW_MODEL
    }
    
    private ObjectAdapter newInstance(ObjectSpecification spec, Variant variant, String memento) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating {} instance of {}", variant, spec);
        }
        final Object pojo;

        if(variant == Variant.VIEW_MODEL) {
            pojo = recreateViewModel(spec, memento);
        } else {
            pojo = objectAdapterContext.instantiateAndInjectServices(spec);

        }

        final ObjectAdapter adapter = objectAdapterContext.getObjectAdapterProvider().adapterFor(pojo);
        return initializePropertiesAndDoCallback(adapter);
    }
    
    private Object recreateViewModel(final ObjectSpecification spec, final String memento) {
        final ViewModelFacet facet = spec.getFacet(ViewModelFacet.class);
        if(facet == null) {
            throw new IllegalArgumentException("spec does not have ViewModelFacet; spec is " + spec.getFullIdentifier());
        }

        final Object viewModelPojo;
        if(facet.getRecreationMechanism().isInitializes()) {
            viewModelPojo = objectAdapterContext.instantiateAndInjectServices(spec);
            facet.initialize(viewModelPojo, memento);
        } else {
            viewModelPojo = facet.instantiate(spec.getCorrespondingClass(), memento);
        }
        return viewModelPojo;
    }
    
    private ObjectAdapter initializePropertiesAndDoCallback(final ObjectAdapter adapter) {

        // initialize new object
        final Stream<ObjectAssociation> fields = adapter.getSpecification()
                .streamAssociations(Contributed.EXCLUDED);
        fields
            .forEach(field->field.toDefault(adapter));
            
        final Object pojo = adapter.getObject();
        servicesInjector.injectServicesInto(pojo);

        CallbackFacet.Util.callCallback(adapter, CreatedCallbackFacet.class);

        if (Command.class.isAssignableFrom(pojo.getClass())) {

            // special case... the command object is created while the transaction is being started and before
            // the event bus service is initialized (nb: we initialize services *within* a transaction).  To resolve
            // this catch-22 situation, we simply suppress the posting of this event for this domain class.

            // this seems the least unpleasant of the various options available:
            // * we could have put a check in the EventBusService to ignore the post if not yet initialized;
            //   however this might hide other genuine errors
            // * we could have used the thread-local in JdoStateManagerForIsis and the "skip(...)" hook in EventBusServiceJdo
            //   to have this event be skipped; but that seems like co-opting some other design
            // * we could have the transaction initialize the EventBusService as a "special case" before creating the Command;
            //   but then do we worry about it being re-init'd later by the ServicesInitializer?

            // so, doing it this way is this simplest, least obscure.

            if(LOG.isDebugEnabled()) {
                LOG.debug("Skipping postEvent for creation of Command pojo");
            }

        } else {
            objectAdapterContext.postLifecycleEventIfRequired(adapter, CreatedLifecycleEventFacet.class);
        }

        return adapter;
    }

    
}