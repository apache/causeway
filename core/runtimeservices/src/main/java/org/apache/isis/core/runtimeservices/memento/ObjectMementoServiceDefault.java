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
package org.apache.isis.core.runtimeservices.memento;

import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMementoCollection;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMementoForEmpty;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMementoService;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 *
 * @since 2.0
 *
 */
@Service
@Named("isis.runtimeservices.ObjectMementoServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class ObjectMementoServiceDefault implements ObjectMementoService {

    @Inject @Getter private SpecificationLoader specificationLoader;
    @Inject private MetaModelContext mmc;
    @Inject private ObjectManager objectManager;

    @Override
    public ObjectMemento mementoForBookmark(@NonNull final Bookmark bookmark) {
//        _Probe.errOut("mementoForRootOid %s", oid);
        val mementoAdapter = _ObjectMemento.createPersistent(bookmark, specificationLoader);
        return ObjectMementoAdapter.of(mementoAdapter);
    }

    @Override
    public ObjectMemento mementoForObject(@Nullable final ManagedObject adapter) {
        assertSingleton(adapter);
//        _Probe.errOut("mementoForObject %s", adapter);
        val mementoAdapter = _ObjectMemento.createOrNull(adapter);
        if(mementoAdapter==null) {
            // sonar-ignore-on (fails to detect this as null guard)
            return ManagedObjects.isSpecified(adapter)
                    ? new ObjectMementoForEmpty(adapter.getSpecification().getLogicalType())
                    : null;
            // sonar-ignore-on
        }
        return ObjectMementoAdapter.of(mementoAdapter);
    }

    @Override
    public ObjectMemento mementoForParameter(@NonNull final ManagedObject paramAdapter) {
//        _Probe.errOut("mementoForParameter %s", paramAdapter);
        assertSingleton(paramAdapter);
        val mementoAdapter = _ObjectMemento.createOrNull(paramAdapter);
        if(mementoAdapter==null) {
            return new ObjectMementoForEmpty(paramAdapter.getSpecification().getLogicalType());
        }
        return ObjectMementoAdapter.of(mementoAdapter);
    }


    @Override
    public ObjectMemento mementoForPojo(final Object pojo) {
        //_Probe.errOut("mementoForPojo %s", ""+pojo);
        assertSingleton(pojo);

        val managedObject = objectManager.adapt(pojo);
        return mementoForObject(managedObject);
    }

    @Override
    public ObjectMemento mementoForPojos(final Iterable<Object> iterablePojos, final LogicalType logicalType) {
//        _Probe.errOut("mementoForPojos");
        val listOfMementos = _NullSafe.stream(iterablePojos)
                .map(pojo->mementoForPojo(pojo))
                .collect(Collectors.toCollection(ArrayList::new)); // ArrayList is serializable

        return ObjectMementoCollection.of(listOfMementos, logicalType);
    }

    @Override
    public ManagedObject reconstructObject(@Nullable final ObjectMemento memento) {

        if(memento==null) {
            return null;
        }

        if(memento instanceof ObjectMementoForEmpty) {
            val objectMementoForEmpty = (ObjectMementoForEmpty) memento;
            val logicalType = objectMementoForEmpty.getLogicalType();
            val spec = specificationLoader.specForLogicalType(logicalType);
            return spec.isPresent()
                    ? ManagedObject.empty(spec.get())
                    : ManagedObject.unspecified();
        }

        if(memento instanceof ObjectMementoCollection) {
            val objectMementoCollection = (ObjectMementoCollection) memento;

            val listOfPojos = objectMementoCollection.unwrapList().stream()
                    .map(this::reconstructObject)
                    .filter(_NullSafe::isPresent)
                    .map(ManagedObject::getPojo)
                    .filter(_NullSafe::isPresent)
                    .collect(Collectors.toCollection(ArrayList::new));

            return ManagedObject.lazy(specificationLoader, listOfPojos);
        }

        if(memento instanceof ObjectMementoAdapter) {
            val objectMementoAdapter = (ObjectMementoAdapter) memento;
            return objectMementoAdapter.reconstructObject(mmc);
        }

        throw _Exceptions.unrecoverableFormatted("unsupported ObjectMemento type %s", memento.getClass());
    }

//TODO 2x remove if no longer required for debugging ...
    private void assertSingleton(final ManagedObject adapter) {
//        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)) {
//            return;
//        }
//        val pojo = ManagedObjects.UnwrapUtil.single(adapter);
//        assertSingleton(pojo);
//        val spec = adapter.getSpecification();
//        if(!spec.isNotCollection()) {
//            throw _Exceptions.illegalArgument("unexpected spec type %s for %s (elementSpec=%s)",
//                    spec, spec.getFullIdentifier(), spec.getElementSpecification());
//        }
    }

    private void assertSingleton(final Object pojo) {
//        if(_NullSafe.streamAutodetect(pojo).limit(2).count()>1L) {
//            throw _Exceptions.illegalArgument("cardinality 0 or 1 expect");
//        }
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class ObjectMementoAdapter implements ObjectMemento {

        private static final long serialVersionUID = 1L;

        private final _ObjectMemento delegate;

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
        public LogicalType getLogicalType() {
            return delegate.getLogicalType();
        }

        ManagedObject reconstructObject(final MetaModelContext mmc) {
            return delegate.reconstructObject(mmc);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

    }

}
