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
package org.apache.isis.metamodel.objectmanager.create;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;

import org.apache.isis.applib.events.lifecycle.AbstractLifecycleEvent;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.factory.InstanceUtil;
import org.apache.isis.metamodel.context.MetaModelContext;
import org.apache.isis.metamodel.facets.object.callbacks.CallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.CreatedCallbackFacet;
import org.apache.isis.metamodel.facets.object.callbacks.CreatedLifecycleEventFacet;
import org.apache.isis.metamodel.facets.object.callbacks.LifecycleEventFacet;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
final class ObjectCreator_builtinHandlers {
    
    @Data @Log4j2
    public static class LegacyCreationHandler implements ObjectCreator.Handler {
        
        private MetaModelContext metaModelContext;
        
        @Override
        public boolean isHandling(ObjectCreator.Request objectCreateRequest) {
            return true;
        }

        @Override
        public ManagedObject handle(ObjectCreator.Request objectCreateRequest) {
            
            //XXX legacy of objectAdapterContext.objectCreationMixin.newInstance(objectSpec);
            
            val spec = objectCreateRequest.getObjectSpecification();
            
            if (log.isDebugEnabled()) {
                log.debug("creating instance of {}", spec);
            }
            
            val pojo = instantiateAndInjectServices(spec);

            val adapter = ManagedObject.of(spec, pojo);
            return initializePropertiesAndDoCallback(adapter);
        }
        
        //  -- HELPER
        
        private Object instantiateAndInjectServices(ObjectSpecification spec) {

            val type = spec.getCorrespondingClass();
            if (type.isArray()) {
                return Array.newInstance(type.getComponentType(), 0);
            }

            if (Modifier.isAbstract(type.getModifiers())) {
                throw _Exceptions.unrecoverable("Cannot create an instance of an abstract class: " + type);
            }

            try {
                
                val newInstance = type.newInstance();
                metaModelContext.getServiceInjector().injectServicesInto(newInstance);
                return newInstance;    
                
            } catch (IllegalAccessException | InstantiationException e) {
                throw _Exceptions.unrecoverable(
                        "Failed to create instance of type " + spec.getFullIdentifier(), e);
            }

        }
        
        private ManagedObject initializePropertiesAndDoCallback(ManagedObject adapter) {

            // initialize new object
            adapter.getSpecification().streamAssociations(Contributed.EXCLUDED)
            .forEach(field->field.toDefault(adapter));

             val pojo = adapter.getPojo();
            
            //XXX pojo already got everything injected above
            //metaModelContext.getServiceInjector().injectServicesInto(pojo);

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
                postLifecycleEventIfRequired(adapter, CreatedLifecycleEventFacet.class);
            }

            return adapter;
        }
        
        private void postLifecycleEventIfRequired(
                ManagedObject adapter,
                Class<? extends LifecycleEventFacet> lifecycleEventFacetClass) {
            
            val lifecycleEventFacet = adapter.getSpecification().getFacet(lifecycleEventFacetClass);
            if(lifecycleEventFacet == null) {
                return;
            }
            
            val eventType = lifecycleEventFacet.getEventType();
            val instance = InstanceUtil.createInstance(eventType);
            val pojo = adapter.getPojo();
            postEvent(_Casts.uncheckedCast(instance), pojo);
            
        }

        private <T> void postEvent(AbstractLifecycleEvent<T> event, T pojo) {
            
            metaModelContext.getServiceRegistry()
                .lookupService(EventBusService.class)
                .ifPresent(eventBusService->{
                    event.initSource(pojo);
                    eventBusService.post(event);
                });
        }
        

    }
    

    // -- NULL GUARD
    
//    @Data
//    public static class GuardAgainstNull implements ObjectCreator.Handler {
//        
//        private MetaModelContext metaModelContext;
//        
//        @Override
//        public boolean isHandling(Request objectLoadRequest) {
//            
//            if(objectLoadRequest==null) {
//                return true;
//            }
//            
//            val spec = objectLoadRequest.getObjectSpecification();
//            if(spec == null) {
//                // eg "NONEXISTENT:123"
//                return true;
//            }
//
//            return false;
//        }
//
//        @Override
//        public ManagedObject handle(Request objectLoadRequest) {
//            return null; // yes null
//        }
//
//    }
    
    // -- MANAGED BEANS

//    @Data
//    public static class LoadService implements ObjectCreator.Handler {
//        
//        private MetaModelContext metaModelContext;
//
//        @Override
//        public boolean isHandling(Request objectLoadRequest) {
//            
//            val spec = objectLoadRequest.getObjectSpecification();
//            return spec.isManagedBean();
//        }
//
//        @Override
//        public ManagedObject handle(Request objectLoadRequest) {
//            
//            val spec = objectLoadRequest.getObjectSpecification();
//            val beanName = spec.getSpecId().asString();
//            
//            val servicePojo = metaModelContext.getServiceRegistry()
//                .lookupRegisteredBeanById(beanName)
//                .map(ManagedBeanAdapter::getInstance)
//                .flatMap(Can::getFirst)
//                .orElseThrow(()->_Exceptions.noSuchElement(
//                        "loader: %s loading beanName %s", 
//                        this.getClass().getName(), beanName));
//            
//            return ManagedObject.of(spec, servicePojo);
//        }
//
//    }
    
    // -- VALUES
    
//    @Data
//    public static class CreateValueDefault implements ObjectCreator.Handler {
//
//        private MetaModelContext metaModelContext;
//        
//        @Override
//        public boolean isHandling(Request objectLoadRequest) {
//            
//            val spec = objectLoadRequest.getObjectSpecification();
//            return spec.isValue();
//        }
//
//        @Override
//        public ManagedObject handle(Request objectLoadRequest) {
//            
//            // cannot load a value
//            
//            val spec = objectLoadRequest.getObjectSpecification();
//            throw _Exceptions.illegalArgument(
//                    "cannot load a value, loader: %s loading ObjectSpecification %s", 
//                        this.getClass().getName(), spec);
//        }
//
//    }
//
//    // -- VIEW MODELS
//    
//    @Data
//    public static class CreateViewModel implements ObjectCreator.Handler {
//        
//        private MetaModelContext metaModelContext;
//
//        @Override
//        public boolean isHandling(Request objectLoadRequest) {
//            
//            val spec = objectLoadRequest.getObjectSpecification();
//            return spec.isViewModel();
//        }
//
//        @Override
//        public ManagedObject handle(Request objectLoadRequest) {
//            
//            val spec = objectLoadRequest.getObjectSpecification();
//            val viewModelFacet = spec.getFacet(ViewModelFacet.class);
//            if(viewModelFacet == null) {
//                throw _Exceptions.illegalArgument(
//                        "ObjectSpecification is missing a ViewModelFacet: %s", spec);
//            }
//            
//            val memento = objectLoadRequest.getObjectIdentifier();
//            final Object viewModelPojo;
//            if(viewModelFacet.getRecreationMechanism().isInitializes()) {
//                viewModelPojo = this.instantiateAndInjectServices(spec);
//                viewModelFacet.initialize(viewModelPojo, memento);
//            } else {
//                viewModelPojo = viewModelFacet.instantiate(spec.getCorrespondingClass(), memento);
//            }
//            
//            return ManagedObject.of(spec, viewModelPojo);
//        }
//        
//        private Object instantiateAndInjectServices(ObjectSpecification spec) {
//
//            val type = spec.getCorrespondingClass();
//            if (type.isArray()) {
//                return Array.newInstance(type.getComponentType(), 0);
//            }
//
//            if (Modifier.isAbstract(type.getModifiers())) {
//                throw _Exceptions.illegalArgument("Cannot create an instance of an abstract class '%s', "
//                        + "creator: %s creating ObjectSpecification %s", 
//                        type, this.getClass().getName(), spec);
//            }
//
//            final Object newInstance;
//            try {
//                newInstance = type.newInstance();
//            } catch (final IllegalAccessException | InstantiationException e) {
//                throw _Exceptions.illegalArgument("Failed to create instance of type '%s', "
//                        + "creator: %s creating ObjectSpecification %s", 
//                        type, this.getClass().getName(), spec);
//            }
//
//            metaModelContext.getServiceInjector().injectServicesInto(newInstance);
//            return newInstance;
//        }
//
//    }
//
//    // -- ENTITIES
//    
//    @Data
//    public static class CreateEntity implements ObjectCreator.Handler {
//        
//        private MetaModelContext metaModelContext;
//
//        @Override
//        public boolean isHandling(Request objectLoadRequest) {
//            
//            val spec = objectLoadRequest.getObjectSpecification();
//            return spec.isEntity();
//        }
//
//        @Override
//        public ManagedObject handle(Request objectLoadRequest) {
//
//            val spec = objectLoadRequest.getObjectSpecification();
//            val entityFacet = spec.getFacet(EntityFacet.class);
//            if(entityFacet==null) {
//                throw _Exceptions.illegalArgument(
//                        "ObjectSpecification is missing an EntityFacet: %s", spec);
//            }
//            
//            val identifier = objectLoadRequest.getObjectIdentifier();
//            val entityPojo = entityFacet.fetchByIdentifier(spec, identifier);
//            
//            metaModelContext.getServiceInjector().injectServicesInto(entityPojo);
//            
//            return ManagedObject.of(spec, entityPojo);
//        }
//
//    }
//    
//    // -- UNKNOWN LOAD REQUEST
//    
//    @Data
//    public static class CreateOther implements ObjectCreator.Handler {
//        
//        private MetaModelContext metaModelContext;
//
//        @Override
//        public boolean isHandling(Request objectLoadRequest) {
//            return true; // the last handler in the chain
//        }
//
//        @Override
//        public ManagedObject handle(Request objectLoadRequest) {
//
//            // unknown object load request
//            
//            throw _Exceptions.illegalArgument(
//                    "unknown object create request, creator: %s creating from ObjectSpecification %s", 
//                        this.getClass().getName(), objectLoadRequest.getObjectSpecification());
//
//        }
//
//    }

}
