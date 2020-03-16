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
package org.apache.isis.core.metamodel.objectmanager.detach;

import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.core.metamodel.objectmanager.refresh.ObjectRefresher;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.Data;
import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
final class ObjectDetacher_builtinHandlers {

    // -- NULL GUARD
    
    @Data
    public static class GuardAgainstNull implements ObjectDetacher.Handler {
        
        private MetaModelContext metaModelContext;
        
        @Override
        public boolean isHandling(ManagedObject managedObject) {
            
            if(managedObject==null || managedObject.getPojo()==null) {
                return true;
            }
            
            return false;
        }

        @Override
        public ManagedObject handle(ManagedObject managedObject) {
            return null; // noop
        }

    }
    
    @Data
    public static class DetachEntity implements ObjectDetacher.Handler {

        private final MetaModelContext metaModelContext;

        @Override
        public boolean isHandling(ManagedObject request) {
            val spec = request.getSpecification();
            return spec.isEntity();
        }

        @Override
        public ManagedObject handle(ManagedObject request) {
            
            val spec = request.getSpecification();
            val entityFacet = spec.getFacet(EntityFacet.class);
            if(entityFacet==null) {
                throw _Exceptions.illegalArgument(
                        "ObjectSpecification is missing an EntityFacet: %s", spec);
            }

            Object detachedPojo = entityFacet.detach(request.getPojo());

            // we assume that we don't need to inject services again, because this should 
            // already have been done, when the entity object got fetched with the ObjectLoader
            
            return metaModelContext.getObjectManager().adapt(detachedPojo);
        }
        
    }
    
    @Data
    public static class DetachOther implements ObjectDetacher.Handler {
        
        @Override
        public boolean isHandling(ManagedObject request) {
            // if no one else feels responsible, we do
            return true;
        }

        @Override
        public ManagedObject handle(ManagedObject request) {
            return request;
        }
        
    }
    
    
    
}
