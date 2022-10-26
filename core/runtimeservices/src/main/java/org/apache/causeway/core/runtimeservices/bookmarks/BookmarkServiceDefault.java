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
package org.apache.causeway.core.runtimeservices.bookmarks;

import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.exceptions.unrecoverable.ObjectNotFoundException;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkHolder;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.val;

/**
 * This service enables a serializable 'bookmark' to be created for an entity.
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".BookmarkServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class BookmarkServiceDefault implements BookmarkService {

    @Inject private SpecificationLoader specificationLoader;
    @Inject private WrapperFactory wrapperFactory;
    @Inject private ObjectManager objectManager;
    @Inject private MetaModelContext mmc;

    @Override
    public Optional<Object> lookup(final @Nullable BookmarkHolder bookmarkHolder) {
        if(bookmarkHolder == null) {
            return Optional.empty();
        }
        val bookmark = bookmarkHolder.getBookmark();
        return bookmark != null
                ? lookup(bookmark)
                : Optional.empty();
    }

    // why would we ever store Service Beans as Bookmarks?
    // - ANSWER: because it might be used by the CommandService to replay a command or exec in the background.
    @Override
    public Optional<Object> lookup(final @Nullable Bookmark bookmark) {
        try {
            return mmc.getObjectManager().loadObject(bookmark)
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
        return objectManager.bookmarkObject(adapter);
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

    // -- HELPER

    private Object unwrapped(final Object domainObject) {
        return wrapperFactory != null
                ? wrapperFactory.unwrap(domainObject)
                : domainObject;
    }

}
