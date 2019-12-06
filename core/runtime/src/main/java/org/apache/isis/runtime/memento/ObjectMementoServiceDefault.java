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
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.objectmanager.ObjectManager;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.specloader.SpecificationLoader;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * 
 * @since 2.0 
 *
 */
@Service
@Named("isisRuntime.ObjectMementoServiceDefault")
@Singleton
public class ObjectMementoServiceDefault
implements ObjectMementoService {
    
    @Inject @Getter private SpecificationLoader specificationLoader;
    @Inject private ObjectManager objectManager;
    private ObjectUnmarshaller objectUnmarshaller;

    @Override
    public ObjectMemento mementoForRootOid(RootOid rootOid) {
        val mementoAdapter = ObjectMementoLegacy.createPersistent(rootOid, specificationLoader);
        return ObjectMementoAdapter.of(mementoAdapter);
    }

    @Override
    public ObjectMemento mementoForAdapter(ManagedObject adapter) {
        val mementoAdapter = ObjectMementoLegacy.createOrNull(adapter);
        if(mementoAdapter==null) {
            return null;
        }
        return ObjectMementoAdapter.of(mementoAdapter);
    }

    @Override
    public ObjectMemento mementoForPojo(Object pojo) {
        val managedObject = objectManager.adapt(pojo);
        return mementoForAdapter(managedObject);
    }

    @Override
    public ManagedObject reconstructObject(ObjectMemento memento) {
        if(memento==null) {
            return null;
        }
        if(objectUnmarshaller==null) {
            objectUnmarshaller = new ObjectUnmarshaller(objectManager, specificationLoader);
        }
        
        return memento.reconstructObject(objectUnmarshaller);
    }

    @RequiredArgsConstructor(staticName = "of")
    static class ObjectMementoAdapter implements ObjectMemento {

        private static final long serialVersionUID = 1L;

        private final ObjectMementoLegacy delegate;

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
        public ManagedObject reconstructObject(ObjectUnmarshaller objectUnmarshaller) {
            return delegate.reconstructObject(objectUnmarshaller, objectUnmarshaller.getSpecificationLoader());
        }
        
        @Override
        public String toString() {
            return delegate.toString();
        }

    }

}
