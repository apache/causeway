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
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.id.HasLogicalType;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.hint.HintIdProvider;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmTitleUtil;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;

@ToString
public final class ObjectMementoForScalar
implements HasLogicalType, Serializable, ObjectMemento {

    private static final long serialVersionUID = 1L;

    // -- FACTORIES

    public static Optional<ObjectMementoForScalar> create(final @Nullable ManagedObject adapter) {
        return ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)
                ? Optional.empty()
                : Optional.of(new ObjectMementoForScalar(adapter));
    }

    static ObjectMementoForScalar createPersistent(
            final Bookmark bookmark,
            final SpecificationLoader specificationLoader) {
        return new ObjectMementoForScalar(bookmark, specificationLoader);
    }

    // --

    @Getter(onMethod_ = {@Override}) final LogicalType logicalType;

    _Recreatable.RecreateStrategy recreateStrategy;

    @Getter(onMethod_ = {@Override}) private final String title;
    @Getter final Bookmark bookmark;

    @ToString.Exclude
    byte[] serializedPayload;

    private ObjectMementoForScalar(
            final @NonNull Bookmark bookmark,
            final @NonNull SpecificationLoader specLoader) {

        this.bookmark = bookmark;

        val logicalTypeName = bookmark.getLogicalTypeName();
        val spec = specLoader.specForLogicalTypeName(logicalTypeName)
                .orElseThrow(()->_Exceptions.unrecoverable(
                        "cannot recreate spec from logicalTypeName %s", logicalTypeName));

        this.logicalType = spec.getLogicalType();

        this.title = "?memento?"; // TODO can we do better?

        this.recreateStrategy = spec.isValue()
                ? _Recreatable.RecreateStrategy.VALUE
                : _Recreatable.RecreateStrategy.LOOKUP;
    }

    private ObjectMementoForScalar(final @NonNull ManagedObject adapter) {

        this.logicalType = adapter.getLogicalType();
        this.title = MmTitleUtil.titleOf(adapter);

        val spec = adapter.getSpecification();

        if(spec.isIdentifiable()
                || spec.isParented() ) {
            val hintId = adapter.getPojo() instanceof HintIdProvider
                 ? ((HintIdProvider) adapter.getPojo()).hintId()
                 : null;

            val bookmark = ManagedObjects.bookmarkElseFail(adapter);
            this.bookmark = hintId != null
                    && bookmark != null
                        ? bookmark.withHintId(hintId)
                        : bookmark;

            recreateStrategy = _Recreatable.RecreateStrategy.LOOKUP;
            return;
        }

        if (spec.isValue()) {
            bookmark = ManagedObjects.bookmarkElseFail(adapter);
            recreateStrategy = _Recreatable.RecreateStrategy.VALUE;
            return;
        }

        throw _Exceptions.illegalArgument("Don't know how to create an ObjectMemento for a type "
                + "with ObjectSpecification %s. "
                + "All other strategies failed. Type is neither "
                + "identifiable (isManagedBean() || isViewModel() || isEntity()), "
                + "nor is a 'parented' Collection, "
                + "nor has 'encodable' semantics, nor is (Serializable || Externalizable)", spec);

    }

    public ManagedObject reconstructObject(final MetaModelContext mmc) {
        val spec = mmc.getSpecificationLoader()
                .specForLogicalType(logicalType).orElse(null);
        if(spec==null) {
            // eg. ill-formed request
            return null;
        }

        // intercept when managed by Spring
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
        if (!(other instanceof ObjectMementoForScalar)) {
            return false;
        }
        return recreateStrategy.equals(this, (ObjectMementoForScalar) other);
    }

}
