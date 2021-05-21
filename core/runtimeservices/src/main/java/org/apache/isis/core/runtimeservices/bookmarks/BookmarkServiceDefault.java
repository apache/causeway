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
package org.apache.isis.core.runtimeservices.bookmarks;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.exceptions.unrecoverable.ObjectNotFoundException;
import org.apache.isis.applib.graph.tree.TreeState;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkHolder;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.memento._Mementos.SerializingAdapter;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.val;

/**
 * This service enables a serializable 'bookmark' to be created for an entity.
 */
@Service
@Named("isis.runtimeservices.BookmarkServiceDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class BookmarkServiceDefault implements BookmarkService, SerializingAdapter {

    @Inject private SpecificationLoader specificationLoader;
    @Inject private WrapperFactory wrapperFactory;
    @Inject private ObjectManager objectManager;
    @Inject private MetaModelContext mmc;

    @Override
    public Optional<Object> lookup(final @Nullable BookmarkHolder bookmarkHolder) {
        if(bookmarkHolder == null) {
            return Optional.empty();
        }
        val bookmark = bookmarkHolder.bookmark();
        return bookmark != null
                ? lookup(bookmark)
                : Optional.empty();
    }

    // why would we ever store Service Beans as Bookmarks?
    // - ANSWER: because it might be used by the CommandService to replay a command or exec in the background.
    @Override
    public Optional<Object> lookup(final @Nullable Bookmark bookmark) {
        try {
            return mmc.loadObject(bookmark)
                    .map(ManagedObject::getPojo);
        } catch(ObjectNotFoundException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Bookmark> bookmarkFor(final @Nullable Object domainObject) {
        if(domainObject == null) {
            return Optional.empty();
        }
        val adapter = objectManager.adapt(unwrapped(domainObject));
        if(!ManagedObjects.isIdentifiable(adapter)){
            // eg values cannot be bookmarked
            return Optional.empty();
        }
        return Optional.of(
                objectManager.bookmarkObject(adapter));
    }

    private Object unwrapped(Object domainObject) {
        return wrapperFactory != null
                ? wrapperFactory.unwrap(domainObject)
                : domainObject;
    }


    @Override
    public Optional<Bookmark> bookmarkFor(
            final @Nullable Class<?> cls,
            final @Nullable String identifier) {

        if(_Strings.isNullOrEmpty(identifier)
                || cls==null) {
            return Optional.empty();
        }
        return specificationLoader.specForType(cls)
                .map(ObjectSpecification::getLogicalType)
                .map(logicalType->Bookmark.forLogicalTypeAndIdentifier(logicalType, identifier));
    }

    // -- SERIALIZING ADAPTER IMPLEMENTATION

    @Override
    public <T> T read(Class<T> cls, Serializable value) {

        if(Bookmark.class.equals(cls)) {
            return _Casts.uncheckedCast(value);
        }

        if(Bookmark.class.isAssignableFrom(value.getClass())) {
            final Bookmark valueBookmark = (Bookmark) value;
            return _Casts.uncheckedCast(lookup(valueBookmark).orElse(null));
        }

        return _Casts.uncheckedCast(value);
    }

    @Override
    public Serializable write(Object value) {
        if(isPredefinedSerializable(value.getClass())) {
            return (Serializable) value;
        } else {
            return bookmarkForElseFail(value);
        }
    }

    // -- HELPER

    private static final Set<Class<? extends Serializable>> serializableFinalTypes = _Sets.of(
            String.class, String[].class,
            Class.class, Class[].class,
            Character.class, Character[].class, char[].class,
            Boolean.class, Boolean[].class, boolean[].class,
            // Numbers
            Byte[].class, byte[].class,
            Short[].class, short[].class,
            Integer[].class, int[].class,
            Long[].class, long[].class,
            Float[].class, float[].class,
            Double[].class, double[].class
            );

    private static final List<Class<? extends Serializable>> serializableTypes = _Lists.of(
            java.util.Date.class,
            java.sql.Date.class,
            Enum.class,
            Bookmark.class,
            TreeState.class
            );

    private static boolean isPredefinedSerializable(final Class<?> cls) {
        if(!Serializable.class.isAssignableFrom(cls)) {
            return false;
        }
        // primitive ... boolean, byte, char, short, int, long, float, and double.
        if(cls.isPrimitive() || Number.class.isAssignableFrom(cls)) {
            return true;
        }
        //[ahuber] any non-scalar values could be problematic, so we are careful with wild-cards here
        if(cls.getName().startsWith("java.time.")) {
            return true;
        }
        if(cls.getName().startsWith("org.joda.time.")) {
            return true;
        }
        if(serializableFinalTypes.contains(cls)) {
            return true;
        }
        return serializableTypes.stream().anyMatch(t->t.isAssignableFrom(cls));
    }


}
