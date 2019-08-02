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

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.ioc.BeanSort;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.runtime.system.context.IsisContext;

import lombok.Getter;
import lombok.Value;
import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
@Value(staticConstructor = "of")
final class ObjectAdapterMementoCollection implements ObjectAdapterMemento {

    private static final long serialVersionUID = 1L;

    private final ArrayList<ObjectAdapterMemento> container; 
    @Getter(onMethod = @__({@Override})) private final ObjectSpecId objectSpecId;

    @Override
    public void resetVersion() {
        throw _Exceptions.notImplemented(); // please unwrap at call-site
    }

    @Override
    public ObjectAdapter getObjectAdapter() {

        //TODO[2112] we don't need the persistence layer to do that!
        val listOfPojos = getContainer().stream()
                .map(ObjectAdapterMemento::getObjectAdapter)
                .filter(_NullSafe::isPresent)
                .map(ObjectAdapter::getPojo)
                .filter(_NullSafe::isPresent)
                .collect(Collectors.toCollection(ArrayList::new));
        return IsisContext.getPersistenceSession().get().adapterFor(listOfPojos);
    }

    @Override
    public String asString() {
        return getContainer().toString();
    }

    @Override
    public Bookmark asHintingBookmarkIfSupported() {
        throw _Exceptions.notImplemented(); // please unwrap at call-site
    }

    @Override
    public Bookmark asBookmarkIfSupported() {
        throw _Exceptions.notImplemented(); // please unwrap at call-site
    }    

    public ArrayList<ObjectAdapterMemento> unwrapList() {
        return getContainer();
    }

    @Override
    public BeanSort getBeanSort() {
        return BeanSort.COLLECTION;
    }

    @Override
    public UUID getStoreKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RootOid getRootOid() {
        // TODO Auto-generated method stub
        return null;
    }

}
