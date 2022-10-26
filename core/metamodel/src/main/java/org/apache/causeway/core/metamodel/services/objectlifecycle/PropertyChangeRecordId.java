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
package org.apache.causeway.core.metamodel.services.objectlifecycle;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode(of = {"bookmarkStr", "propertyId"})
@ToString(of = {"bookmarkStr", "propertyId"})
public final class PropertyChangeRecordId {

    @Getter private final String bookmarkStr;
    @Getter private final String propertyId;

    @Getter private final ManagedObject entity;
    @Getter private final Bookmark bookmark;
    @Getter private OneToOneAssociation property;

    public static PropertyChangeRecordId of(
            final @NonNull ManagedObject entity,
            final @NonNull OneToOneAssociation property) {
        return new PropertyChangeRecordId(entity, property);
    }
    private PropertyChangeRecordId(
            final ManagedObject entity,
            final OneToOneAssociation property) {

        // these exposed as a convenience
        this.entity = entity;
        this.property = property;
        this.bookmark = ManagedObjects.bookmarkElseFail(entity);

        // these are the key
        this.bookmarkStr = bookmark.toString();
        this.propertyId = property.getId();

    }

}

