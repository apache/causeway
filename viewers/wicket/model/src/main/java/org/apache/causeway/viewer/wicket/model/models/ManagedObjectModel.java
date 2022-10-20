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
package org.apache.causeway.viewer.wicket.model.models;

import java.util.Objects;
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.PackedManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.util.Facets;

import lombok.NonNull;
import lombok.Synchronized;
import lombok.val;

/**
 * @since 2.0
 */
public final class ManagedObjectModel
extends ModelAbstract<ManagedObject> {

    private static final long serialVersionUID = 1L;

    private ObjectMemento memento;

    protected ManagedObjectModel(
            @NonNull final MetaModelContext commonContext) {
        this(commonContext, null);
    }

    protected ManagedObjectModel(
            @NonNull final MetaModelContext commonContext,
            @Nullable final ObjectMemento initialMemento) {

        super(commonContext);
        this.memento = initialMemento;
    }


    @Override
    protected ManagedObject load() {
        if (memento == null) {
            return null;
        }
        return getObjectManager().demementify(memento);
    }

    @Override
    public void setObject(final ManagedObject adapter) {

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)) {
            super.setObject(null);
            memento = null;
            return;
        }

        super.setObject(adapter);

        if(adapter instanceof PackedManagedObject) {
            setObjectCollection((PackedManagedObject)adapter);
        } else {
            memento = adapter.getMemento().orElseThrow();
        }
    }

    public void setObjectCollection(final PackedManagedObject adapter) {

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)) {
            super.setObject(null);
            memento = null;
            return;
        }

        super.setObject(adapter);
        memento = adapter.getMemento().orElseThrow();
    }

    public final Bookmark asBookmarkIfSupported() {
        return memento!=null
                ? memento.getBookmark()
                : null;
    }

    public final String oidStringIfSupported() {
        return memento!=null
                ? memento.toString()
                : null;
    }

    /**
     * free of side-effects, used for serialization
     * @implNote overriding this must be consistent with {@link #getTypeOfSpecification()}
     */
    public Optional<LogicalType> getLogicalElementType() {
        return Optional.ofNullable(memento)
                .map(ObjectMemento::getLogicalType);
    }

    private transient ObjectSpecification elementTypeSpec;
    private transient boolean isObjectSpecMemoized = false;
    /**
     * @implNote can be overridden by sub-models (eg {@link ScalarModel}) that know the type of
     * the adapter without there being one. Overriding this must be consistent
     * with {@link #getLogicalElementType()}
     */
    @Synchronized
    public ObjectSpecification getTypeOfSpecification() {
        if(!isObjectSpecMemoized) {
            val logicalType = getLogicalElementType().orElse(null);
            elementTypeSpec = super.getSpecificationLoader().specForLogicalType(logicalType).orElse(null);
            isObjectSpecMemoized = true;
        }
        return elementTypeSpec;
    }


    public final boolean hasAsRootPolicy() {
        return Facets.bookmarkPolicyMatches(BookmarkPolicy.AS_ROOT::equals)
                .test(getTypeOfSpecification());
    }

    public final boolean hasAsChildPolicy() {
        return Facets.bookmarkPolicyMatches(BookmarkPolicy.AS_CHILD::equals)
                .test(getTypeOfSpecification());
    }

    public boolean isEmpty() {
        return memento == null;
    }

    // -- CONTRACT

    @Override
    public final int hashCode() {
        return Objects.hashCode(memento);
    }

    @Override
    public final boolean equals(final Object obj) {
        if(obj instanceof ManagedObjectModel) {
            val other = (ManagedObjectModel) obj;
            return Objects.equals(this.memento, other.memento);
        }
        return false;
    }

    // -- DEPRECATIONS

//    private ObjectMemento memento() {
//        return memento;
//    }
//
//    private void memento(final ObjectMemento memento) {
//        val manageObject = super.getMetaModelContext().reconstructObject(memento);
//        super.setObject(manageObject);
//        this.memento = memento;
//        this.elementTypeSpec = null; // invalidate
//    }

}
