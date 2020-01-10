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
package org.apache.isis.persistence.jdo.datanucleus5.objectadapter;

import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facets.object.callbacks.CallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.CreatedCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.CreatedLifecycleEventFacet;
import org.apache.isis.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtime.context.session.RuntimeContext;

import lombok.extern.log4j.Log4j2;

/**
 * package private mixin for ObjectAdapterContext
 * <p>
 * Responsibility: re-creates domain object instances  
 * </p> 
 * @since 2.0
 */
@Log4j2
class ObjectAdapterContext_ObjectCreation {

    private final ObjectAdapterContext objectAdapterContext;
    
    private ServiceInjector serviceInjector;

    ObjectAdapterContext_ObjectCreation(
            ObjectAdapterContext objectAdapterContext,
            RuntimeContext runtimeContext) {

        this.objectAdapterContext = objectAdapterContext;
        this.serviceInjector = runtimeContext.getServiceInjector();
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
        if (log.isDebugEnabled()) {
            log.debug("creating {} instance of {}", variant, spec);
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

        final Object pojo = adapter.getPojo();
        serviceInjector.injectServicesInto(pojo);

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

            if(log.isDebugEnabled()) {
                log.debug("Skipping postEvent for creation of Command pojo");
            }

        } else {
            objectAdapterContext.postLifecycleEventIfRequired(adapter, CreatedLifecycleEventFacet.class);
        }

        return adapter;
    }


}