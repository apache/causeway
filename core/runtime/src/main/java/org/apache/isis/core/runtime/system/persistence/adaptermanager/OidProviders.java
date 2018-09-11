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

import java.util.UUID;

import org.apache.isis.core.metamodel.IsisJdoMetamodelPlugin;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.persistence.adaptermanager.factories.OidFactory.OidProvider;

public class OidProviders {

    
    public static class GuardAgainstRootOid implements OidProvider {

        @Override
        public boolean isHandling(Object pojo, ObjectSpecification spec) {
            return pojo instanceof RootOid;
        }

        @Override
        public RootOid oidFor(Object pojo, ObjectSpecification spec) {
            throw new IllegalArgumentException("Cannot create a RootOid for pojo, "
                    + "when pojo is instance of RootOid. You might want to ask "
                    + "ObjectAdapterByIdProvider for an ObjectAdapter instead.");
        }

    }
    
    
    public static class OidForServices implements OidProvider {

        @Override
        public boolean isHandling(Object pojo, ObjectSpecification spec) {
            return spec.isService();
        }

        @Override
        public RootOid oidFor(Object pojo, ObjectSpecification spec) {
            final String identifier = PersistenceSession.SERVICE_IDENTIFIER;
            return new RootOid(spec.getSpecId(), identifier, Oid.State.PERSISTENT);
        }

    }

    public static class OidForPersistent implements OidProvider {

        private final IsisJdoMetamodelPlugin isisJdoMetamodelPlugin = IsisJdoMetamodelPlugin.get();

        @Override
        public boolean isHandling(Object pojo, ObjectSpecification spec) {
            // equivalent to 'isInstanceOfPersistable = pojo instanceof Persistable'
            final boolean isInstanceOfPersistable = isisJdoMetamodelPlugin.isPersistenceEnhanced(pojo.getClass());
            return isInstanceOfPersistable;
        }

        @Override
        public RootOid oidFor(Object pojo, ObjectSpecification spec) {
            final PersistenceSession persistenceSession = IsisContext.getPersistenceSession().get();
            final boolean isRecognized = persistenceSession.isRecognized(pojo);
            if(isRecognized) {
                final String identifier = persistenceSession.identifierFor(pojo);
                return new RootOid(spec.getSpecId(), identifier, Oid.State.PERSISTENT);
            } else {
                final String identifier = UUID.randomUUID().toString();
                return new RootOid(spec.getSpecId(), identifier, Oid.State.TRANSIENT);    
            }
        }
        
    }

    public static class OidForValues implements OidProvider {

        @Override
        public boolean isHandling(Object pojo, ObjectSpecification spec) {
            return spec.containsFacet(ValueFacet.class);
        }

        @Override
        public RootOid oidFor(Object pojo, ObjectSpecification spec) {
            return RootOid.value();
        }

    }
    
    public static class OidForViewModels implements OidProvider {

        @Override
        public boolean isHandling(Object pojo, ObjectSpecification spec) {
            return spec.containsFacet(ViewModelFacet.class);
        }

        @Override
        public RootOid oidFor(Object pojo, ObjectSpecification spec) {
            final ViewModelFacet recreatableObjectFacet = spec.getFacet(ViewModelFacet.class);
            final String identifier = recreatableObjectFacet.memento(pojo);
            return new RootOid(spec.getSpecId(), identifier, Oid.State.VIEWMODEL);
        }

    }
    
    public static class OidForOthers implements OidProvider {

        @Override
        public boolean isHandling(Object pojo, ObjectSpecification spec) {
            return true; // try to handle anything
        }

        @Override
        public RootOid oidFor(Object pojo, ObjectSpecification spec) {
            final String identifier = UUID.randomUUID().toString();
            return new RootOid(spec.getSpecId(), identifier, Oid.State.TRANSIENT);
        }

    }
    

}
