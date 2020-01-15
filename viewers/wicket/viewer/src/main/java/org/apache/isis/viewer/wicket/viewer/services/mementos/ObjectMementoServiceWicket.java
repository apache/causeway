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
package org.apache.isis.viewer.wicket.viewer.services.mementos;

import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.webapp.context.memento.ObjectMemento;
import org.apache.isis.core.webapp.context.memento.ObjectMementoCollection;
import org.apache.isis.core.webapp.context.memento.ObjectMementoService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * 
 * @since 2.0 
 *
 */
@Service
@Named("isisWicketViewer.ObjectMementoServiceWicket")
@Order(OrderPrecedence.MIDPOINT)
@Qualifier("Wicket")
@Singleton
public class ObjectMementoServiceWicket implements ObjectMementoService {

    @Inject @Getter private SpecificationLoader specificationLoader;
    @Inject private ObjectManager objectManager;

    @Override
    public ObjectMemento mementoForRootOid(RootOid rootOid) {
        val mementoAdapter = ObjectMementoLegacy.createPersistent(rootOid, specificationLoader);
        return ObjectMementoAdapter.of(mementoAdapter);
    }

    @Override
    public ObjectMemento mementoForObject(ManagedObject adapter) {
        val mementoAdapter = ObjectMementoLegacy.createOrNull(adapter);
        if(mementoAdapter==null) {
            return null;
        }
        return ObjectMementoAdapter.of(mementoAdapter);
    }

    @Override
    public ObjectMemento mementoForPojo(Object pojo) {
        val managedObject = objectManager.adapt(pojo);
        return mementoForObject(managedObject);
    }
    
    @Override
    public ObjectMemento mementoForPojos(Iterable<Object> iterablePojos, ObjectSpecId specId) {
        val listOfMementos = _NullSafe.stream(iterablePojos)
                .map(pojo->mementoForPojo(pojo))
                .collect(Collectors.toCollection(ArrayList::new)); // ArrayList is serializable

        return ObjectMementoCollection.of(listOfMementos, specId);
    }

    @Override
    public ManagedObject reconstructObject(ObjectMemento memento) {
        if(memento==null) {
            return null;
        }

        if(memento instanceof ObjectMementoCollection) {
            val objectMementoCollection = (ObjectMementoCollection) memento;

            val listOfPojos = objectMementoCollection.unwrapList().stream()
                    .map(this::reconstructObject)
                    .filter(_NullSafe::isPresent)
                    .map(ManagedObject::getPojo)
                    .filter(_NullSafe::isPresent)
                    .collect(Collectors.toCollection(ArrayList::new));

            return ManagedObject.of(specificationLoader::loadSpecification, listOfPojos);
        }

        if(memento instanceof ObjectMementoAdapter) {
            val objectMementoAdapter = (ObjectMementoAdapter) memento;
            return objectMementoAdapter.reconstructObject(specificationLoader);
        }

        throw _Exceptions.unrecoverableFormatted("unsupported ObjectMemento type %s", memento.getClass());
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class ObjectMementoAdapter implements ObjectMemento {

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

        ManagedObject reconstructObject(SpecificationLoader specificationLoader) {
            return delegate.reconstructObject(specificationLoader);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

    }



}
