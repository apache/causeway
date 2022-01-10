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
package org.apache.isis.viewer.wicket.model.models;

import java.util.Objects;
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Collections;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.PackedManagedObject;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;

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
            @NonNull final IsisAppCommonContext commonContext) {
        this(commonContext, null);
    }

    protected ManagedObjectModel(
            @NonNull final IsisAppCommonContext commonContext,
            @Nullable final ObjectMemento initialMemento) {

        super(commonContext);
        this.memento = initialMemento;
    }


    @Override
    protected ManagedObject load() {
        if (memento == null) {
            return null;
        }
        return super.getCommonContext().reconstructObject(memento);
    }

    @Override
    public void setObject(final ManagedObject adapter) {

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)) {
            super.setObject(null);
            memento = null;
            return;
        }

        super.setObject(adapter);

        if(_Collections.isCollectionOrArrayOrCanType(adapter.getPojo().getClass())) {
            setObjectCollection((PackedManagedObject)adapter);
        } else {
            memento = super.getMementoService().mementoForObject(adapter);
        }
    }

    public void setObjectCollection(final PackedManagedObject adapter) {

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)) {
            super.setObject(null);
            memento = null;
            return;
        }

        super.setObject(adapter);

        val pojos = adapter.getPojo();
        memento = super.getMementoService()
                .mementoForPojos(_Casts.uncheckedCast(pojos), getLogicalElementType()
                        .orElseGet(()->adapter.getElementSpecification().get().getLogicalType()));
    }

    public final Bookmark asHintingBookmarkIfSupported() {
        return memento!=null
                ? memento.asHintingBookmarkIfSupported()
                : null;
    }

    public final Bookmark asBookmarkIfSupported() {
        return memento!=null
                ? memento.asBookmarkIfSupported()
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


    public boolean hasAsRootPolicy() {
        return hasBookmarkPolicy(BookmarkPolicy.AS_ROOT);
    }

    public boolean hasAsChildPolicy() {
        return hasBookmarkPolicy(BookmarkPolicy.AS_CHILD);
    }

    private boolean hasBookmarkPolicy(final BookmarkPolicy policy) {
        return lookupFacet(BookmarkPolicyFacet.class)
                .map(facet->facet.value() == policy)
                .orElse(false);
    }

    public <T extends Facet> Optional<T> lookupFacet(final Class<T> facetClass) {
        return Optional.ofNullable(getTypeOfSpecification())
                .map(objectSpec->objectSpec.getFacet(facetClass));
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
//        val manageObject = super.getCommonContext().reconstructObject(memento);
//        super.setObject(manageObject);
//        this.memento = memento;
//        this.elementTypeSpec = null; // invalidate
//    }

}
