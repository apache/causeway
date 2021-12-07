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

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.handler.ChainOfResponsibility;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.spec.ManagedObject;

/**
 * @since 2.0
 */
public interface ObjectBookmarker {

    Bookmark bookmarkObject(ManagedObject managedObject);

    // -- HANDLER

    public interface Handler extends ChainOfResponsibility.Handler<ManagedObject, Bookmark> {}

    // -- FACTORY

    public static ObjectBookmarker createDefault() {
        return managedObject ->
            ChainOfResponsibility.named(
                    "ObjectBookmarker",
                    _Lists.of(
                            new ObjectBookmarker_builtinHandlers.GuardAgainstOid(),
                            new ObjectBookmarker_builtinHandlers.BookmarkForServices(),
                            new ObjectBookmarker_builtinHandlers.BookmarkForValues(),
                            new ObjectBookmarker_builtinHandlers.BookmarkForSerializable(),
                            new ObjectBookmarker_builtinHandlers.BookmarkForViewModels(),
                            new ObjectBookmarker_builtinHandlers.BookmarkForEntities(),
                            new ObjectBookmarker_builtinHandlers.BookmarkForOthers()))
            .handle(managedObject);
    }

}
