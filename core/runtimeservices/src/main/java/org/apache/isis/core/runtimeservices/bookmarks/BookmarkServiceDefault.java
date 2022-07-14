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
import java.util.UUID;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.exceptions.unrecoverable.ObjectNotFoundException;
import org.apache.isis.applib.graph.tree.TreeState;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkHolder;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.bookmark.IdStringifier;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.exceptions._Exceptions;
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
@Priority(PriorityPrecedence.MIDPOINT)
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

    private Object unwrapped(final Object domainObject) {
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

    @Override
    public Bookmark bookmarkForElseFail(final @Nullable Object domainObject) {
        return bookmarkFor(domainObject)
                .orElseThrow(
                        ()->_Exceptions.illegalArgument(
                        "cannot create bookmark for type %s",
                        domainObject!=null
                            ? specificationLoader.specForType(domainObject.getClass())
                                    .map(spec->spec.toString())
                                    .orElseGet(()->domainObject.getClass().getName())
                            : "<null>"));
    }

    // -- SERIALIZING ADAPTER IMPLEMENTATION

    @Override
    public <T> T read(final Class<T> cls, final Serializable value) {
        if (stringifiers != null) {
            for (IdStringifier<?> serializer : stringifiers) {
                if (serializer.handles(cls)) {
                    return (T) serializer.parse((String)value);
                }
            }
        }


        if(Bookmark.class.equals(cls)) {
            return _Casts.uncheckedCast(value);
        }

        if(Bookmark.class.isAssignableFrom(value.getClass())) {
            final Bookmark valueAsBookmark = (Bookmark) value;
            return _Casts.uncheckedCast(lookup(valueAsBookmark).orElse(null));
        }

        return _Casts.uncheckedCast(value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Serializable write(final Object value) {
        if (stringifiers != null) {
            for (IdStringifier stringifier : stringifiers) {
                if (stringifier.handles(value.getClass())) {
                    return stringified(stringifier, value);
                }
            }
        }

        return bookmarkForElseFail(value);
    }

    private static <T> String stringified(IdStringifier<T> stringifier, T value) {
        return stringifier.stringify(value);
    }

    // -- HELPER

    @Inject List<IdStringifier<?>> stringifiers;

}
