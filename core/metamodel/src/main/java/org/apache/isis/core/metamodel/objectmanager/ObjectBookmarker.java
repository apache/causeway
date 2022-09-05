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
package org.apache.isis.core.metamodel.objectmanager;

import java.util.List;
import java.util.Optional;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.Oid;
import org.apache.isis.commons.handler.ChainOfResponsibility;
import org.apache.isis.core.metamodel.object.Bookmarkable;
import org.apache.isis.core.metamodel.object.ManagedObject;

/**
 * @since 2.0
 */
public interface ObjectBookmarker {

    Optional<Bookmark> bookmarkObject(ManagedObject managedObject);

    // -- HANDLER

    public interface Handler extends ChainOfResponsibility.Handler<ManagedObject, Optional<Bookmark>> {}

    // -- FACTORY

    public static ObjectBookmarker createDefault() {
        return managedObject ->
            ChainOfResponsibility.named(
                    "ObjectBookmarker",
                    handlers)
            .handle(managedObject);
    }

    // -- HANDLERS

    static final List<Handler> handlers = List.of(BuiltinHandlers.values());

    enum BuiltinHandlers implements Handler {
        GuardAgainstOid {
            @Override public boolean isHandling(final ManagedObject managedObject) {
                return managedObject.getPojo() instanceof Oid;
            }
            @Override
            public Optional<Bookmark> handle(final ManagedObject managedObject) {
                throw new IllegalArgumentException("Cannot create a Bookmark for pojo, "
                        + "when pojo is instance of Bookmark. You might want to ask "
                        + "ObjectAdapterByIdProvider for an ObjectAdapter instead.");
            }
        },
        BookmarkForBookmarkable {
            @Override
            public boolean isHandling(final ManagedObject managedObject) {
                return managedObject instanceof Bookmarkable;
            }
            @Override
            public Optional<Bookmark> handle(final ManagedObject managedObject) {
                return managedObject.getBookmark();
            }
        },
        BookmarkForOthers {
            @Override
            public boolean isHandling(final ManagedObject managedObject) {
                return true; // try to handle anything
            }
            @Override
            public Optional<Bookmark> handle(final ManagedObject managedObject) {
                return Optional.empty();
            }
        };
    }

}
