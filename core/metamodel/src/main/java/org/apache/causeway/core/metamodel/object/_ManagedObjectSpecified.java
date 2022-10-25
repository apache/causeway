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

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facets.object.title.TitleRenderRequest;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMementoCollection;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMementoForEmpty;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMementoForScalar;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.Accessors;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
abstract class _ManagedObjectSpecified
implements ManagedObject {

    @Getter(onMethod_ = {@Override}) @Accessors(makeFinal = true)
    private final @NonNull Specialization specialization;

    @Getter(onMethod_ = {@Override}) @Accessors(makeFinal = true)
    private final @NonNull ObjectSpecification specification;

    @Override
    public final MetaModelContext getMetaModelContext() {
        return getSpecification().getMetaModelContext();
    }

    @Override
    public final Supplier<ManagedObject> asSupplier() {
        return ()->this;
    }

    @Override
    public final <T> T assertCompliance(final @NonNull T pojo) {
        MmAssertionUtil.assertPojoNotWrapped(pojo);
        if(specification.isAbstract()) {
            _Assert.assertFalse(specialization.getTypePolicy().isExactTypeRequired(),
                    ()->String.format("Specialization %s does not allow abstract type %s",
                            specialization,
                            specification));
        }
        if(specialization.getTypePolicy().isExactTypeRequired()) {
            MmAssertionUtil.assertExactType(specification, pojo);
        }
        if(getSpecialization().getInjectionPolicy().isAlwaysInject()) {
            if(!isInjectionPointsResolved()) {
                getServiceInjector().injectServicesInto(pojo); // might be redundant
            }
        }
        return pojo;
    }

    /**
     * override if there is optimization available
     * @apiNote must only be called by {@link #assertCompliance(Object)}
     */
    protected boolean isInjectionPointsResolved() { return false; }

    @Override
    public String getTitle() {
        return _InternalTitleUtil.titleString(
                TitleRenderRequest.forObject(this));
    }

    @Override
    public Optional<ObjectMemento> getMemento() {
        return this instanceof PackedManagedObject
                ? Optional.ofNullable(mementoForPacked((PackedManagedObject)this))
                : Optional.ofNullable(mementoForScalar(this));
    }

    private ObjectMemento mementoForScalar(@Nullable final ManagedObject adapter) {
        MmAssertionUtil.assertPojoIsScalar(adapter);
        return ObjectMementoForScalar.create(adapter)
                .map(ObjectMemento.class::cast)
                .orElseGet(()->
                ManagedObjects.isSpecified(adapter)
                ? new ObjectMementoForEmpty(adapter.getLogicalType())
                        : null);
    }

    private ObjectMemento mementoForPacked(@NonNull final PackedManagedObject packedAdapter) {
        val listOfMementos = packedAdapter.unpack().stream()
                .map(this::mementoForScalar)
                .collect(Collectors.toCollection(ArrayList::new)); // ArrayList is serializable
        return ObjectMementoCollection.of(
                listOfMementos,
                packedAdapter.getLogicalType());
    }

    //XXX compares pojos by their 'equals' semantics -
    // note though: some value-types have an explicit order-relation which could potentially say differently
    @Override
    public final boolean equals(final Object obj) {
        // make sure equals(Object) is without side-effects!
        if(this == obj) {
            return true;
        }
        if(!(obj instanceof ManagedObject)) {
            return false;
        }
        val other = (ManagedObject)obj;
        if(!this.getSpecialization().equals(other.getSpecialization())) {
            return false;
        }
        if(!this.getSpecification().equals(other.getSpecification())) {
            return false;
        }
        val canGetPojosWithoutSideeffect = !getSpecialization().getPojoPolicy().isRefetchable();
        if(canGetPojosWithoutSideeffect) {
            // expected to work for packed variant just fine, as it compares lists
            return Objects.equals(this.getPojo(), other.getPojo());
        }

        if(this.isBookmarkMemoized()
                && other.isBookmarkMemoized()) {
            return Objects.equals(
                    sideEffectFreeBookmark(),
                    other.getBookmark().orElseThrow(_Exceptions::unexpectedCodeReach));
        }

        val a = (_Refetchable) this;
        val b = (_Refetchable) this;
        return Objects.equals(a.peekAtPojo(), b.peekAtPojo());
    }

    @Override
    public final int hashCode() {
        // make sure hashCode() is without side-effects!
        val canGetPojosWithoutSideeffect = !getSpecialization().getPojoPolicy().isRefetchable();
        return canGetPojosWithoutSideeffect
                // expected to work for packed variant just fine, as it compares lists
                ? Objects.hash(getSpecification().getCorrespondingClass(), getPojo())
                : Objects.hash(getSpecification().getCorrespondingClass(), sideEffectFreeBookmark());
    }

    @Override
    public final String toString() {
        // make sure toString() is without side-effects!
        return String.format("ManagedObject(%s, spec=%s, pojo=%s)",
                getSpecialization().name(),
                getSpecification(),
                !getSpecialization().getPojoPolicy().isRefetchable()
                    ? getPojo() // its safe to get pojo side-effect free
                    : isBookmarkMemoized()
                        ? String.format("(refetchable, %s)", sideEffectFreeBookmark())
                        : "(refetchable, suppressed to not cause side effects)");
    }

    // -- HELPER

    private Bookmark sideEffectFreeBookmark() {
        _Assert.assertTrue(isBookmarkMemoized());
        return getBookmark().orElseThrow(_Exceptions::unexpectedCodeReach);
    }

}
