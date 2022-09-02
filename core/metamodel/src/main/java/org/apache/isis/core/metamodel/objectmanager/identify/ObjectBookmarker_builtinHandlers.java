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

import java.util.UUID;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.Oid;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.object.PackedManagedObject;
import org.apache.isis.core.metamodel.objectmanager.identify.ObjectBookmarker.Handler;

import lombok.SneakyThrows;
import lombok.val;

class ObjectBookmarker_builtinHandlers {

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

    static class BookmarkForNonScalar implements Handler {

        @Override
        public boolean isHandling(final ManagedObject managedObject) {
            return managedObject instanceof PackedManagedObject;
        }

        @Override
        public Bookmark handle(final ManagedObject managedObject) {
            return bookmarkWithRandomUUID(managedObject);
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
            return managedObject.getBookmark()
                    .orElseGet(()->bookmarkWithRandomUUID(managedObject)); // transient
        }

    }

    static class BookmarkForValues implements Handler {

        @Override
        public boolean isHandling(final ManagedObject managedObject) {
            return managedObject.getSpecialization().isValue();
        }

        @SneakyThrows
        @Override
        public Bookmark handle(final ManagedObject managedObject) {
            _Assert.assertTrue(managedObject.isBookmarkSupported(), ()->"is bookmarkable");
            return managedObject.getBookmark().orElseThrow();
        }

    }

    static class BookmarkForViewModels implements Handler {

        @Override
        public boolean isHandling(final ManagedObject managedObject) {
            return managedObject.getSpecialization().isViewmodel();
        }

        @Override
        public Bookmark handle(final ManagedObject managedObject) {

            if(managedObject.isBookmarkMemoized()) {
                return managedObject.getBookmark().get();
            }

            val spec = managedObject.getSpecification();
            return spec.viewmodelFacetElseFail().serializeToBookmark(managedObject);
        }

    }

    static class BookmarkForOthers implements Handler {

        @Override
        public boolean isHandling(final ManagedObject managedObject) {
            return true; // try to handle anything
        }

        @Override
        public Bookmark handle(final ManagedObject managedObject) {
            return bookmarkWithRandomUUID(managedObject);
        }
    }

    // -- HELPER

    private static Bookmark bookmarkWithRandomUUID(final ManagedObject managedObject) {
        val uuid = UUID.randomUUID().toString();
        System.err.printf("called bookmarkWithRandomUUID %s [%s]%n", managedObject.getSpecification(), uuid);
        return managedObject.createBookmark(UUID.randomUUID().toString());
    }

}
