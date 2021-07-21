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
package org.apache.isis.core.metamodel.objectmanager.query;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.NonNull;
import lombok.Value;
import lombok.val;

/**
 *
 * @since 2.0
 *
 */
final class ObjectBulkLoader_builtinHandlers {

    // -- NULL GUARD

    @Value
    public static class GuardAgainstNull implements ObjectBulkLoader.Handler {

        private final @NonNull MetaModelContext metaModelContext;

        @Override
        public boolean isHandling(final ObjectBulkLoader.Request objectQuery) {

            if(objectQuery==null) {
                return true;
            }

            val spec = objectQuery.getObjectSpecification();
            if(spec == null) {
                // eg "NONEXISTENT:123"
                return true;
            }

            // we don't guard against the identifier being null, because, this is ok
            // for services and values
            return false;
        }

        @Override
        public Can<ManagedObject> handle(final ObjectBulkLoader.Request objectQuery) {
            return Can.empty();
        }

    }

    // -- ENTITIES

    @Value
    public static class BulkLoadEntity implements ObjectBulkLoader.Handler {

        private final @NonNull MetaModelContext metaModelContext;

        @Override
        public boolean isHandling(final ObjectBulkLoader.Request objectQuery) {

            val spec = objectQuery.getObjectSpecification();
            return spec.isEntity();
        }

        @Override
        public Can<ManagedObject> handle(final ObjectBulkLoader.Request objectQuery) {

            val spec = objectQuery.getObjectSpecification();
            val entityFacet = spec.getFacet(EntityFacet.class);
            if(entityFacet==null) {
                throw _Exceptions.illegalArgument(
                        "ObjectSpecification is missing an EntityFacet: %s", spec.getCorrespondingClass());
            }

            val entities = entityFacet.fetchByQuery(spec, objectQuery.getQuery());
            val serviceInjector = metaModelContext.getServiceInjector();

            //TODO injection should have already be done by DataNucleus
            entities.map(ManagedObject::getPojo).forEach(serviceInjector::injectServicesInto);
            return entities;
        }

    }

    // -- UNKNOWN LOAD REQUEST

    @Value
    public static class LoadOther implements ObjectBulkLoader.Handler {

        private final @NonNull MetaModelContext metaModelContext;

        @Override
        public boolean isHandling(final ObjectBulkLoader.Request objectQuery) {
            return true; // the last handler in the chain
        }

        @Override
        public Can<ManagedObject> handle(final ObjectBulkLoader.Request objectQuery) {

            // unknown object load request

            throw _Exceptions.illegalArgument(
                    "unknown bulk object load request, loader: %s loading ObjectSpecification %s",
                        this.getClass().getName(), objectQuery.getObjectSpecification());

        }

    }

}
