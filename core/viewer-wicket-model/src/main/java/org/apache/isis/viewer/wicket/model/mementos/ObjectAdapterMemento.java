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

package org.apache.isis.viewer.wicket.model.mementos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento_Legacy.Sort;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento_Legacy.Type;

import lombok.val;

//FIXME [2033] move this (including its implementation) to 'runtime'
public interface ObjectAdapterMemento extends Serializable {

    String asString();
    
    /**
     * Returns a bookmark only if {@link Type#PERSISTENT} and 
     * {@link #getSort() sort} is {@link Sort#SCALAR scalar}.
     * Returns {@code null} otherwise. 
     */
    Bookmark asBookmarkIfSupported();
    
    /**
     * Returns a bookmark only if {@link Type#PERSISTENT} and 
     * {@link #getSort() sort} is {@link Sort#SCALAR scalar}.
     * Returns {@code null} otherwise. 
     */
    Bookmark asHintingBookmarkIfSupported();
    
    ObjectSpecId getObjectSpecId();
    ArrayList<ObjectAdapterMemento> getList();
    
    ObjectAdapter getObjectAdapter(); 
    
    void resetVersion();
    
    // -- DEPRECATIONS
    
//  @Deprecated
//  default ObjectAdapter getObjectAdapter(
//          ConcurrencyChecking noCheck, 
//          PersistenceSession persistenceSession,
//          SpecificationLoader specificationLoader) {
//      return getObjectAdapter();
//  }
    
    // -- FACTORIES

    static ObjectAdapterMemento ofRootOid(RootOid rootOid) {
        return ObjectAdapterMemento_Legacy.ofRootOid(rootOid);
    }

    static ObjectAdapterMemento ofAdapter(ObjectAdapter adapter) {
        return ObjectAdapterMemento_Legacy.ofAdapter(adapter);
    }
    
    static ObjectAdapterMemento ofPojo(Object pojo) {
        val isisSession = IsisSession.currentOrElseNull();
        val adapterProvider = isisSession.getObjectAdapterProvider();
        val objectAdapter = adapterProvider.adapterFor(pojo);
        return ofAdapter(objectAdapter);
    }

    static ObjectAdapterMemento ofMementoList(
            Collection<ObjectAdapterMemento> modelObject, 
            ObjectSpecId specId) {
        
        return ObjectAdapterMemento_Legacy.ofMementoList(modelObject, specId);
    }
    
    static ObjectAdapterMemento ofIterablePojos(
            final Object iterablePojos,
            ObjectSpecId specId) {
        
        final List<ObjectAdapterMemento> listOfMementos = _NullSafe.stream((Iterable<?>) iterablePojos)
                .map(ObjectAdapterMemento::ofPojo)
                .collect(Collectors.toList());
        final ObjectAdapterMemento memento =
                ObjectAdapterMemento.ofMementoList(listOfMementos, specId);
        return memento;
    }
    
    
    // -- 

}
