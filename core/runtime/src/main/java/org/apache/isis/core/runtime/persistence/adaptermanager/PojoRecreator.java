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
package org.apache.isis.core.runtime.persistence.adaptermanager;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.objectstore.jdo.datanucleus.DataNucleusObjectStore;

public class PojoRecreator {

    public Object recreatePojo(RootOid oid) {
        if(oid.isTransient() || oid.isViewModel()) {
            return recreatePojoDefault(oid);
        } else {
            return getObjectStore().loadPojo(oid);
        }
    }

    private Object recreatePojoDefault(final RootOid rootOid) {
        final ObjectSpecification spec = getSpecificationLoader().lookupBySpecId(rootOid.getObjectSpecId());
        final Object pojo = spec.createObject();
        if(rootOid.isViewModel()) {
            // initialize the view model pojo from the oid's identifier

            final ViewModelFacet facet = spec.getFacet(ViewModelFacet.class);
            if(facet == null) {
                throw new IllegalArgumentException("spec does not have RecreatableObjectFacet; " + rootOid.toString() + "; spec is " + spec.getFullIdentifier());
            }

            final String memento = rootOid.getIdentifier();

            facet.initialize(pojo, memento);
        }
        return pojo;
    }




    /**
     * Return an adapter, if possible, for a pojo that was instantiated by the
     * object store as a result of lazily loading, but which hasn't yet been seen
     * by the Isis framework.
     *
     * <p>
     * For example, in the case of JDO object store, downcast to <tt>PersistenceCapable</tt>
     * and 'look inside' its state.
     */
    public ObjectAdapter lazilyLoaded(Object pojo) {
        return getObjectStore().lazilyLoaded(pojo);
    }

    ///////////////////////////////


    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected DataNucleusObjectStore getObjectStore() {
        return (DataNucleusObjectStore) getPersistenceSession().getObjectStore();
    }


}
