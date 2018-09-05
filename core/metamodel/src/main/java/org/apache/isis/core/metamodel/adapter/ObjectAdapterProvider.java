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
package org.apache.isis.core.metamodel.adapter;

import java.util.List;
import java.util.function.Function;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

/**
 * 
 * @since 2.0.0-M2
 *
 */
public interface ObjectAdapterProvider {
    
    // -- INTERFACE

    /**
     * @param pojo
     * @return oid for the given domain object 
     */
    Oid oidFor(Object domainObject);
    
    /**
     * @return standalone (value) or root adapter
     */
    ObjectAdapter adapterFor(Object domainObject);

    /**
     * @return collection adapter.
     */
    ObjectAdapter adapterFor(
            final Object domainObject,
            final ObjectAdapter parentAdapter,
            OneToManyAssociation collection);

    /**
     * @param viewModelPojo
     * @return an ObjectAdapter 'bypassing mapping', that holds the ObjectSpecification
     * FIXME[ISIS-1976] Note: whether or not 'bypassing mapping' should not be exposed by the API.
     * So this further needs refactoring. 
     */
    ObjectAdapter specificationForViewModel(final Object viewModelPojo);

    ObjectAdapter adapterForViewModel(
            final Object viewModelPojo, 
            final Function<ObjectSpecId, RootOid> rootOidFactory);
    

    List<ObjectAdapter> getServices();
    
    
    // -- FOR THOSE THAT IMPLEMENT THROUGH DELEGATION
    
    public static interface Delegating extends ObjectAdapterProvider {
        
        @Programmatic
        ObjectAdapterProvider getObjectAdapterProvider();
        
        @Programmatic
        default Oid oidFor(Object domainObject) {
            return getObjectAdapterProvider().oidFor(domainObject);
        }
        
        @Programmatic
        default ObjectAdapter adapterFor(Object domainObject) {
            return getObjectAdapterProvider().adapterFor(domainObject);
        }

        @Programmatic
        default ObjectAdapter adapterFor(
                final Object pojo,
                final ObjectAdapter parentAdapter,
                OneToManyAssociation collection) {
            return getObjectAdapterProvider().adapterFor(pojo, parentAdapter, collection);
        }

        @Programmatic
        default ObjectAdapter specificationForViewModel(final Object viewModelPojo) {
            return getObjectAdapterProvider().specificationForViewModel(viewModelPojo);
        }

        @Programmatic
        default ObjectAdapter adapterForViewModel(
                final Object viewModelPojo, 
                final Function<ObjectSpecId, RootOid> rootOidFactory) {
            return getObjectAdapterProvider().adapterForViewModel(viewModelPojo, rootOidFactory);
        }
        
        @Programmatic
        default List<ObjectAdapter> getServices() {
            return getObjectAdapterProvider().getServices();
        }
        
    }


    
    

}
