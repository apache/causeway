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

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.ioc.BeanSort;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.persistence.PersistenceSession;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * 
 * @deprecated TODO[2112] adapter to LAST KNOWN GOOD, but does not work
 *
 */
public class ObjectAdapterMementoSupport_usingLKG 
implements ObjectAdapterMementoSupport {

    @Override
    public ObjectAdapterMemento mementoForRootOid(RootOid rootOid) {
        val delegate = ObjectAdapterMemento_LastKnownGood.createPersistent(rootOid);
        return ObjectAdapterMementoDelegator.of(delegate);
    }

    @Override
    public ObjectAdapterMemento mementoForAdapter(ObjectAdapter adapter) {
        val delegate = ObjectAdapterMemento_LastKnownGood.createOrNull(adapter);
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
    public ObjectAdapter reconstructObjectAdapter(ObjectAdapterMemento memento) {
        return memento.getObjectAdapter();
    }

    @RequiredArgsConstructor(staticName = "of")
    static class ObjectAdapterMementoDelegator implements ObjectAdapterMemento {

        private static final long serialVersionUID = 1L;
        
        private final ObjectAdapterMemento_LastKnownGood delegate;
        
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
        public ObjectAdapter getObjectAdapter() {
            return delegate.getObjectAdapter(ConcurrencyChecking.NO_CHECK, 
                    persistenceSession(), 
                    specificationLoader());
        }

        @Override
        public void resetVersion() {
            delegate.resetVersion(persistenceSession(), specificationLoader());
        }
        
        private PersistenceSession persistenceSession() {
            return IsisContext.getPersistenceSession().get();
        }

        private SpecificationLoader specificationLoader() {
            return IsisContext.getSpecificationLoader();
        }
        
    }

}
