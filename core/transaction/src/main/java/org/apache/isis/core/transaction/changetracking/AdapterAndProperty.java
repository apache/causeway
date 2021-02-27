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
package org.apache.isis.core.transaction.changetracking;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import lombok.experimental.PackagePrivate;

@EqualsAndHashCode(of = {"bookmarkStr", "propertyId"})
@PackagePrivate
@ToString(of = {"bookmarkStr", "propertyId"})
final class AdapterAndProperty {

    @Getter private final ManagedObject adapter;
    @Getter private final ObjectAssociation property;
    @Getter private final Bookmark bookmark;
    @Getter private final String propertyId;
    
    private final String bookmarkStr;

    public static AdapterAndProperty of(
            final @NonNull ManagedObject adapter, 
            final @NonNull ObjectAssociation property) {
        return new AdapterAndProperty(adapter, property);
    }

    private AdapterAndProperty(ManagedObject adapter, ObjectAssociation property) {
        
        this.adapter = adapter;
        this.property = property;
        this.propertyId = property.getId();

        this.bookmark = ManagedObjects.bookmarkElseFail(adapter);
        this.bookmarkStr = bookmark.toString();
        
    }

    public String getMemberId() {
        return property.getIdentifier().getClassAndMemberNameIdentityString();
    }

    Object getPropertyValue() {
        val referencedAdapter = property.get(adapter, InteractionInitiatedBy.FRAMEWORK);
        return referencedAdapter == null ? null : referencedAdapter.getPojo();
    }

    
}
