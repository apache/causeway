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
package org.apache.causeway.core.metamodel.objectmanager.memento;

import java.io.Serializable;

import org.apache.causeway.applib.id.HasLogicalType;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.hint.HintIdProvider;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmTitleUtils;

import lombok.Builder;
import lombok.NonNull;

@Builder
record ObjectMementoForScalar(
        @NonNull LogicalType logicalType,
        @NonNull Bookmark bookmark,
        String title)
implements HasLogicalType, Serializable, ObjectMemento {

    // -- FACTORIES

    static ObjectMementoForScalar create(final @NonNull ManagedObject adapter) {

        var builder = ObjectMementoForScalar.builder()
                .logicalType(adapter.getLogicalType())
                .title(MmTitleUtils.titleOf(adapter));

        var spec = adapter.getSpecification();

        if(spec.isIdentifiable()
                || spec.isParented() ) {
            var hintId = adapter.getPojo() instanceof HintIdProvider
                 ? ((HintIdProvider) adapter.getPojo()).hintId()
                 : null;

            var bookmark = ManagedObjects.bookmarkElseFail(adapter);
            bookmark = hintId != null
                    && bookmark != null
                        ? bookmark.withHintId(hintId)
                        : bookmark;

            builder.bookmark(bookmark);

            return builder.build();
        }

        if (spec.isValue()) {
            builder.bookmark(ManagedObjects.bookmarkElseFail(adapter));
            return builder.build();
        }

        throw _Exceptions.illegalArgument("Don't know how to create an ObjectMemento for a type "
                + "with ObjectSpecification %s. "
                + "All other strategies failed. Type is neither "
                + "identifiable (isManagedBean() || isViewModel() || isEntity()), "
                + "nor is a 'parented' Collection, "
                + "nor has 'encodable' semantics, nor is (Serializable || Externalizable)", spec);

    }

    @Override public Bookmark getBookmark() { return bookmark; }
    @Override public String getTitle() { return title; }
    @Override public LogicalType getLogicalType() { return logicalType; }

    @Override public int hashCode() { return bookmark.hashCode(); }

    @Override public boolean equals(final Object o) {
        return (o instanceof ObjectMementoForScalar other)
                ? this.bookmark.equals(other.bookmark)
                : false;
    }

}
