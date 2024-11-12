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

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.object.MMHintUtils;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmTitleUtils;

import lombok.NonNull;

record ObjectMementoSingular(
        @NonNull LogicalType logicalType,
        @NonNull Bookmark bookmark,
        String title)
implements ObjectMemento {

    // -- FACTORIES

    static ObjectMementoSingular create(final @NonNull ManagedObject adapter) {
        var spec = adapter.getSpecification();

        _Assert.assertTrue(spec.isIdentifiable()
                || spec.isParented()
                || spec.isValue(), ()->"Don't know how to create an ObjectMemento for a type "
                        + "with ObjectSpecification %s. "
                        + "All other strategies failed. Type is neither "
                        + "identifiable (isManagedBean() || isViewModel() || isEntity()), "
                        + "nor is a 'parented' Collection, "
                        + "nor has 'encodable' semantics, nor is (Serializable || Externalizable)"
                        .formatted(spec));

        return new ObjectMementoSingular(
                adapter.getLogicalType(),
                MMHintUtils.bookmarkElseFail(adapter),
                MmTitleUtils.titleOf(adapter));
    }

    @Override public int hashCode() { return bookmark.hashCode(); }
    @Override public boolean equals(final Object o) {
        return (o instanceof ObjectMementoSingular other)
                ? this.bookmark.equals(other.bookmark)
                : false;
    }

}
