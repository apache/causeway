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
package org.apache.isis.runtime.memento;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.ioc.BeanSort;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.persistence.PersistenceSession;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * 
 * @since 2.0 
 *
 */
@Service @Singleton
public class ObjectAdapterMementoSupport_usingDefault 
implements ObjectAdapterMementoSupport {
    
    @Inject @Getter private SpecificationLoader specificationLoader;

    @Override
    public ObjectAdapterMemento mementoForRootOid(RootOid rootOid) {
        val delegate = ObjectAdapterMementoDefault.createPersistent(rootOid, specificationLoader);
        return ObjectAdapterMementoDelegator.of(delegate);
    }

    @Override
    public ObjectAdapterMemento mementoForAdapter(ManagedObject adapter) {
        val delegate = ObjectAdapterMementoDefault.createOrNull(adapter);
        if(delegate==null) {
            return null;
        }
        return ObjectAdapterMementoDelegator.of(delegate);
    }

    @Override
    public ObjectAdapterMemento mementoForPojo(Object pojo) {
        val ps = IsisContext.getPersistenceSession().get();
        val adapter = ps.adapterFor(pojo);
        return mementoForAdapter(adapter);
    }

    @Override
    public ManagedObject reconstructObjectAdapter(ObjectAdapterMemento memento) {
        return memento.getObjectAdapter(specificationLoader);
    }

    @RequiredArgsConstructor(staticName = "of")
    static class ObjectAdapterMementoDelegator implements ObjectAdapterMemento {

        private static final long serialVersionUID = 1L;

        private final ObjectAdapterMementoDefault delegate;

        @Override
        public UUID getStoreKey() {
            return null;
        }

        @Override
        public BeanSort getBeanSort() {
            return null;
        }

        @Override
        public RootOid getRootOid() {
            return null;
        }

        @Override
        public String asString() {
            return delegate.asString();
        }

        @Override
        public Bookmark asBookmarkIfSupported() {
            return delegate.asBookmark();
        }

        @Override
        public Bookmark asHintingBookmarkIfSupported() {
            return delegate.asHintingBookmark();
        }

        @Override
        public ObjectSpecId getObjectSpecId() {
            return delegate.getObjectSpecId();
        }

        @Override
        public ObjectAdapter getObjectAdapter(SpecificationLoader specificationLoader) {
            return delegate.getObjectAdapter(persistenceSession(), specificationLoader);
        }
        
        private PersistenceSession persistenceSession() {
            return IsisContext.getPersistenceSession().get();
        }

    }

}
