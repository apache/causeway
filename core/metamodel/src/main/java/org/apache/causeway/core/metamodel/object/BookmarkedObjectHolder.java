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
package org.apache.causeway.core.metamodel.object;

import java.io.Serializable;
import java.util.Objects;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;

/**
 * Transiently holds a detachable {@link ManagedObject},
 * that is lazily created based on the given immutable {@link Bookmark}.
 */
public final class BookmarkedObjectHolder implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Bookmark bookmark;
    private transient ManagedObject managedObject;

    public BookmarkedObjectHolder(final @NonNull Bookmark bookmark, final @Nullable ManagedObject managedObject) {
        this.bookmark = bookmark;
        this.managedObject = managedObject;
    }

    public Bookmark bookmark() {
        return bookmark;
    }

    public ManagedObject managedObject() {
        return managedObject!=null
            ? managedObject
            : (this.managedObject = objectManager().loadObjectElseFail(bookmark));
    }

    public void detach() {
        this.managedObject = null;
    }

    // -- CONTRACT

    @Override
    public final int hashCode() {
        return bookmark.hashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        return obj instanceof BookmarkedObjectHolder other
            ? Objects.equals(this.bookmark, other.bookmark)
            : false;
    }

    @Override
    public String toString() {
        return "ManagedObjectHolder[%s]".formatted(bookmark);
    }

    // -- HELPER

    private ObjectManager objectManager() {
        return MetaModelContext.instanceElseFail().getObjectManager();
    }

}
