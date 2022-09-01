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
package org.apache.isis.core.metamodel.objectmanager.identify;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.Oid;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.object.PackedManagedObject;
import org.apache.isis.core.metamodel.objectmanager.identify.ObjectBookmarker.Handler;

import lombok.SneakyThrows;
import lombok.val;

class ObjectBookmarker_builtinHandlers {

    public static final String SERVICE_IDENTIFIER = "1";

    static class GuardAgainstOid implements Handler {

        @Override
        public boolean isHandling(final ManagedObject managedObject) {
            return managedObject.getPojo() instanceof Oid;
        }

        @Override
        public Bookmark handle(final ManagedObject managedObject) {
            throw new IllegalArgumentException("Cannot create a Bookmark for pojo, "
                    + "when pojo is instance of Bookmark. You might want to ask "
                    + "ObjectAdapterByIdProvider for an ObjectAdapter instead.");
        }

    }

    static class BookmarkForServices implements Handler {

        @Override
        public boolean isHandling(final ManagedObject managedObject) {
            return managedObject.getSpecialization().isService();
        }

        @Override
        public Bookmark handle(final ManagedObject managedObject) {
            return managedObject.getBookmark().orElseThrow();
        }
    }

    static class BookmarkForEntities implements Handler {

        @Override
        public boolean isHandling(final ManagedObject managedObject) {
            return managedObject.getSpecialization().isEntity();
        }

        @Override
        public Bookmark handle(final ManagedObject managedObject) {
            return managedObject.getBookmark().orElseThrow();
        }

    }

    static class BookmarkForValues implements Handler {

        @Override
        public boolean isHandling(final ManagedObject managedObject) {
            return managedObject.getSpecification().isValue();
        }

        @SneakyThrows
        @Override
        public Bookmark handle(final ManagedObject managedObject) {
            val spec = managedObject.getSpecification();
            val valuePojo = managedObject.getPojo();
            if(valuePojo==null) {
                return Bookmark.forLogicalTypeAndIdentifier(spec.getLogicalType(), "{}");
            }

            val valueFacet = spec.valueFacet().orElse(null);
            ValueSemanticsProvider<Object> composer = (ValueSemanticsProvider) valueFacet.selectDefaultSemantics()
                    .orElseThrow(()->_Exceptions.illegalArgument(
                            "Cannot create a bookmark for the value type %s, "
                          + "as no appropriate ValueSemanticsProvider could be found.",
                          managedObject.getSpecification().getCorrespondingClass().getName()));

            val valueAsJson = composer.decompose(managedObject.getPojo())
                    .toJson();

            val identifier = _Strings.ofBytes(
                    _Bytes.asUrlBase64.apply(valueAsJson.getBytes()),
                    StandardCharsets.UTF_8);

            return Bookmark.forLogicalTypeAndIdentifier(spec.getLogicalType(), identifier);
        }

    }

    static class BookmarkForViewModels implements Handler {

        @Override
        public boolean isHandling(final ManagedObject managedObject) {
            return (managedObject instanceof PackedManagedObject)
                    ? false
                    : managedObject.getSpecification().containsFacet(ViewModelFacet.class);
        }

        @Override
        public Bookmark handle(final ManagedObject managedObject) {

            if(managedObject.isBookmarkMemoized()) {
                return managedObject.getBookmark().get();
            }

            val spec = managedObject.getSpecification();
            val recreatableObjectFacet = spec.getFacet(ViewModelFacet.class);
            return recreatableObjectFacet.serializeToBookmark(managedObject);
        }

    }

    static class BookmarkForOthers implements Handler {

        @Override
        public boolean isHandling(final ManagedObject managedObject) {
            return true; // try to handle anything
        }

        @Override
        public Bookmark handle(final ManagedObject managedObject) {
            val spec = managedObject.getSpecification();
            val identifier = UUID.randomUUID().toString();
            return Bookmark.forLogicalTypeAndIdentifier(spec.getLogicalType(), identifier);
        }
    }

}
