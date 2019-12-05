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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.objectmanager.ObjectManager;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.system.context.IsisContext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * 
 * @since 2.0 
 *
 */
@Service @Singleton
public class ObjectAdapterMementoServiceDefault 
implements ObjectAdapterMementoService {
    
    @Inject @Getter private SpecificationLoader specificationLoader;
    @Inject private ObjectManager objectManager;
    private MementoStore mementoStore;

    @Override
    public ObjectAdapterMemento mementoForRootOid(RootOid rootOid) {
        val delegate = MementoHelper.createPersistent(rootOid, specificationLoader);
        return ObjectAdapterMementoDelegator.of(delegate);
    }

    @Override
    public ObjectAdapterMemento mementoForAdapter(ManagedObject adapter) {
        val delegate = MementoHelper.createOrNull(adapter);
        if(delegate==null) {
            return null;
        }
        return ObjectAdapterMementoDelegator.of(delegate);
    }

    @Override
    public ObjectAdapterMemento mementoForPojo(Object pojo) {
        val adapter = objectManager.adapt(pojo);
        return mementoForAdapter(adapter);
    }

    @Override
    public ManagedObject reconstructObject(ObjectAdapterMemento memento) {
        if(memento==null) {
            return null;
        }
        if(mementoStore==null) {
            mementoStore = new MementoStoreLegacy(objectManager, specificationLoader);
        }
        
        return memento.reconstructObject(mementoStore, specificationLoader);
    }

    @RequiredArgsConstructor(staticName = "of")
    static class ObjectAdapterMementoDelegator implements ObjectAdapterMemento {

        private static final long serialVersionUID = 1L;

        private final MementoHelper delegate;

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
        public ManagedObject reconstructObject(
                MementoStore mementoStore, SpecificationLoader specificationLoader) {
            
            return delegate.getObjectAdapter(mementoStore, specificationLoader);
        }
        
        @Override
        public String toString() {
            return delegate.toString();
        }

    }

}
