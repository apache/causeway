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
package org.apache.isis.runtime.persistence.transaction;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

import lombok.val;

public class AdapterAndProperty {

    private final ObjectAdapter objectAdapter;
    private final ObjectAssociation property;
    private final Bookmark bookmark;
    private final String propertyId;
    private final String bookmarkStr;

    public static AdapterAndProperty of(ObjectAdapter adapter, ObjectAssociation property) {
        return new AdapterAndProperty(adapter, property);
    }

    private AdapterAndProperty(ObjectAdapter adapter, ObjectAssociation property) {
        this.objectAdapter = adapter;
        this.property = property;

        final RootOid oid = (RootOid) adapter.getOid();
        
        oid.asBookmark();

        final String objectType = oid.getObjectSpecId().asString();
        final String identifier = oid.getIdentifier();
        bookmark = new Bookmark(objectType, identifier);
        bookmarkStr = bookmark.toString();

        propertyId = property.getId();
    }

    public ObjectAdapter getAdapter() {
        return objectAdapter;
    }

    public ObjectAssociation getProperty() {
        return property;
    }

    public Bookmark getBookmark() {
        return bookmark;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public String getMemberId() {
        return property.getIdentifier().toClassAndNameIdentityString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final AdapterAndProperty that = (AdapterAndProperty) o;

        if (bookmarkStr != null ? !bookmarkStr.equals(that.bookmarkStr) : that.bookmarkStr != null)
            return false;
        if (propertyId != null ? !propertyId.equals(that.propertyId) : that.propertyId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = propertyId != null ? propertyId.hashCode() : 0;
        result = 31 * result + (bookmarkStr != null ? bookmarkStr.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return bookmarkStr + " , " + getProperty().getId();
    }

    Object getPropertyValue() {
        val referencedAdapter = property.get(objectAdapter, InteractionInitiatedBy.FRAMEWORK);
        return referencedAdapter == null ? null : referencedAdapter.getPojo();
    }


}
