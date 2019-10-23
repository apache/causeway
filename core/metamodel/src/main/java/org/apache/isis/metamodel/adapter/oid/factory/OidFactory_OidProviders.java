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
package org.apache.isis.metamodel.adapter.oid.factory;

import java.util.UUID;

import org.apache.isis.config.SystemConstants;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.adapter.oid.factory.OidFactory.OidProvider;
import org.apache.isis.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.metamodel.spec.ManagedObject;

import lombok.val;

class OidFactory_OidProviders {


    static class GuardAgainstRootOid implements OidProvider {

        @Override
        public boolean isHandling(ManagedObject managedObject) {
            return managedObject.getPojo() instanceof RootOid;
        }

        @Override
        public RootOid oidFor(ManagedObject managedObject) {
            throw new IllegalArgumentException("Cannot create a RootOid for pojo, "
                    + "when pojo is instance of RootOid. You might want to ask "
                    + "ObjectAdapterByIdProvider for an ObjectAdapter instead.");
        }

    }

    static class OidForServices implements OidProvider {

        @Override
        public boolean isHandling(ManagedObject managedObject) {
            return managedObject.getSpecification().isManagedBean();
        }

        @Override
        public RootOid oidFor(ManagedObject managedObject) {
            final String identifier = SystemConstants.SERVICE_IDENTIFIER;
            return Oid.Factory.persistentOf(managedObject.getSpecification().getSpecId(), identifier);
        }

    }

    static class OidForEntities implements OidProvider {

        @Override
        public boolean isHandling(ManagedObject managedObject) {
            return managedObject.getSpecification().isEntity();
        }

        @Override
        public RootOid oidFor(ManagedObject managedObject) {
            val spec = managedObject.getSpecification();
            val pojo = managedObject.getPojo();
            val entityFacet = spec.getFacet(EntityFacet.class);
            val identifier = entityFacet.identifierFor(pojo);
            return Oid.Factory.persistentOf(spec.getSpecId(), identifier);
        }

    }

    static class OidForValues implements OidProvider {

        @Override
        public boolean isHandling(ManagedObject managedObject) {
            return managedObject.getSpecification().containsFacet(ValueFacet.class);
        }

        @Override
        public RootOid oidFor(ManagedObject managedObject) {
            return Oid.Factory.value();
        }

    }

    static class OidForViewModels implements OidProvider {

        @Override
        public boolean isHandling(ManagedObject managedObject) {
            return managedObject.getSpecification().containsFacet(ViewModelFacet.class);
        }

        @Override
        public RootOid oidFor(ManagedObject managedObject) {
            val spec = managedObject.getSpecification();
            val recreatableObjectFacet = spec.getFacet(ViewModelFacet.class);
            val identifier = recreatableObjectFacet.memento(managedObject.getPojo());
            return Oid.Factory.viewmodelOf(spec.getSpecId(), identifier);
        }

    }

    static class OidForOthers implements OidProvider {

        @Override
        public boolean isHandling(ManagedObject managedObject) {
            return true; // try to handle anything
        }

        @Override
        public RootOid oidFor(ManagedObject managedObject) {
            val spec = managedObject.getSpecification();
            val identifier = UUID.randomUUID().toString();
            return Oid.Factory.transientOf(spec.getSpecId(), identifier);
        }

    }


}
