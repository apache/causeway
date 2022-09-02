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
package org.apache.isis.core.runtimeservices.memento;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.id.HasLogicalType;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.hint.HintIdProvider;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.object.ManagedObjects;
import org.apache.isis.core.metamodel.object.MmTitleUtil;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

final class _ObjectMemento implements HasLogicalType, Serializable {

    private static final long serialVersionUID = 1L;

    // -- FACTORIES

    public static Optional<_ObjectMemento> create(final @Nullable ManagedObject adapter) {
        return ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)
                ? Optional.empty()
                : Optional.of(new _ObjectMemento(adapter));
    }

    static _ObjectMemento createPersistent(
            final Bookmark bookmark,
            final SpecificationLoader specificationLoader) {
        return new _ObjectMemento(bookmark, specificationLoader);
    }

    // --

    @Getter(onMethod_ = {@Override}) final LogicalType logicalType;

    transient _Recreatable.RecreateStrategy recreateStrategy;

    byte[] serializedObject;
    String stringifiedBookmark;

    private Bookmark bookmark;
    private String titleString;

    private _ObjectMemento(final Bookmark bookmark, final SpecificationLoader specLoader) {

        val logicalTypeName = bookmark.getLogicalTypeName();
        val spec = specLoader.specForLogicalTypeName(logicalTypeName)
                .orElseThrow(()->_Exceptions.unrecoverable(
                        "cannot recreate spec from logicalTypeName %s", logicalTypeName));

        this.logicalType = spec.getLogicalType();
        this.stringifiedBookmark = bookmark.stringify();
        Objects.requireNonNull(stringifiedBookmark, "stringifiedBookmark");


        if(spec.isValue()) {
            this.recreateStrategy = _Recreatable.RecreateStrategy.VALUE;
            return;
        }

        this.bookmark = bookmark;
        this.recreateStrategy = _Recreatable.RecreateStrategy.LOOKUP;
    }

    private _ObjectMemento(final @NonNull ManagedObject adapter) {
        val spec = adapter.getSpecification();
        this.logicalType = spec.getLogicalType();
        init(adapter);
    }

    private void init(final ManagedObject adapter) {

        titleString = MmTitleUtil.titleOf(adapter);

        val spec = adapter.getSpecification();

        if(spec.isIdentifiable() || spec.isParented() ) {
            val hintId = adapter.getPojo() instanceof HintIdProvider
                 ? ((HintIdProvider) adapter.getPojo()).hintId()
                 : null;

            bookmark = ManagedObjects.bookmarkElseFail(adapter);
            bookmark = hintId != null
                    && bookmark != null
                        ? bookmark.withHintId(hintId)
                        : bookmark;

            stringifiedBookmark = bookmark.stringify();
            recreateStrategy = _Recreatable.RecreateStrategy.LOOKUP;
            return;
        }

        if (spec.isValue()) {
            bookmark = ManagedObjects.bookmarkElseFail(adapter);
            stringifiedBookmark = bookmark.stringify();
            recreateStrategy = _Recreatable.RecreateStrategy.VALUE;
            return;
        }

        if(spec.isSerializable()) {
            val serializer = spec.getMetaModelContext().getObjectManager().getObjectSerializer();
            serializedObject = serializer.serialize(adapter);
            recreateStrategy = _Recreatable.RecreateStrategy.SERIALIZABLE;
            return;
        }

        throw _Exceptions.illegalArgument("Don't know how to create an ObjectMemento for a type "
                + "with ObjectSpecification %s. "
                + "All other strategies failed. Type is neither "
                + "identifiable (isManagedBean() || isViewModel() || isEntity()), "
                + "nor is a 'parented' Collection, "
                + "nor has 'encodable' semantics, nor is (Serializable || Externalizable)", spec);

    }

    public String getTitleString() {
        return titleString;
    }

    public Bookmark asBookmark() {
        val bookmark = asStrictBookmark();
        return bookmark!=null
                ? bookmark
                : asPseudoBookmark();
    }

    private Bookmark asStrictBookmark() {
        return bookmark;
    }

    private Bookmark asPseudoBookmark() {
        return recreateStrategy.asPseudoBookmark(this);
    }

    ManagedObject reconstructObject(final MetaModelContext mmc) {

        val specificationLoader = mmc.getSpecificationLoader();
        val spec = specificationLoader.specForLogicalType(logicalType).orElse(null);
        if(spec==null) {
            // eg. ill-formed request
            return null;
        }

        // intercept when managed by IoCC
        if(spec.getBeanSort().isManagedBeanAny()) {
            return spec.getMetaModelContext().lookupServiceAdapterById(getLogicalTypeName());
        }

        return recreateStrategy.recreateObject(this, mmc);
    }

    @Override
    public int hashCode() {
        return recreateStrategy.hashCode(this);
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof _ObjectMemento)) {
            return false;
        }
        return recreateStrategy.equals(this, (_ObjectMemento) other);
    }

}
