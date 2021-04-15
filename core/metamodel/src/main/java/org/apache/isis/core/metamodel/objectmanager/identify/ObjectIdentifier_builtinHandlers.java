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
package org.apache.isis.core.metamodel.objectmanager.identify;

import java.util.UUID;

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.objectmanager.identify.ObjectIdentifier.Handler;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.val;

class ObjectIdentifier_builtinHandlers {

    public static final String SERVICE_IDENTIFIER = "1";

    static class GuardAgainstRootOid implements Handler {

        @Override
        public boolean isHandling(ManagedObject managedObject) {
            return managedObject.getPojo() instanceof Oid;
        }

        @Override
        public Oid handle(ManagedObject managedObject) {
            throw new IllegalArgumentException("Cannot create a RootOid for pojo, "
                    + "when pojo is instance of RootOid. You might want to ask "
                    + "ObjectAdapterByIdProvider for an ObjectAdapter instead.");
        }

    }

    static class OidForServices implements Handler {

        @Override
        public boolean isHandling(ManagedObject managedObject) {
            return managedObject.getSpecification().isManagedBean();
        }

        @Override
        public Oid handle(ManagedObject managedObject) {
            final String identifier = SERVICE_IDENTIFIER;
            return Oid.of(managedObject.getSpecification().getLogicalType(), identifier);
        }

    }

    static class OidForEntities implements Handler {

        @Override
        public boolean isHandling(ManagedObject managedObject) {
            return managedObject.getSpecification().isEntity();
        }

        @Override
        public Oid handle(ManagedObject managedObject) {
            val spec = managedObject.getSpecification();
            val pojo = managedObject.getPojo();
            if(pojo==null) {
                val msg = String.format("entity '%s' is null, cannot identify", managedObject);
                throw _Exceptions.unrecoverable(msg);
            }
            val entityFacet = spec.getFacet(EntityFacet.class);
            if(entityFacet==null) {
                val msg = String.format("entity '%s' has no EntityFacet associated", managedObject);
                throw _Exceptions.unrecoverable(msg);
            }
            val identifier = entityFacet.identifierFor(spec, pojo);
            return Oid.of(spec.getLogicalType(), identifier);
        }

    }

    static class OidForValues implements Handler {

        @Override
        public boolean isHandling(ManagedObject managedObject) {
            return managedObject.getSpecification().containsFacet(ValueFacet.class);
        }

        @Override
        public Oid handle(ManagedObject managedObject) {
            return Oid.empty();
        }

    }

    static class OidForViewModels implements Handler {

        @Override
        public boolean isHandling(ManagedObject managedObject) {
            return managedObject.getSpecification().containsFacet(ViewModelFacet.class);
        }

        @Override
        public Oid handle(ManagedObject managedObject) {
            val spec = managedObject.getSpecification();
            val recreatableObjectFacet = spec.getFacet(ViewModelFacet.class);
            val identifier = recreatableObjectFacet.memento(managedObject.getPojo());
            return Oid.of(spec.getLogicalType(), identifier);
        }

    }

    static class OidForOthers implements Handler {

        @Override
        public boolean isHandling(ManagedObject managedObject) {
            return true; // try to handle anything
        }

        @Override
        public Oid handle(ManagedObject managedObject) {
            val spec = managedObject.getSpecification();
            val identifier = UUID.randomUUID().toString();
            return Oid.of(spec.getLogicalType(), identifier);
        }

    }


}
