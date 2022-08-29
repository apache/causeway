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
package org.apache.isis.core.metamodel.object;

import java.util.Objects;
import java.util.function.Supplier;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
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

    /** debug */
    @Override
    public final void assertSpecIsInSyncWithPojo() {
//        val pojo = getPojo();
//        val spec = getSpecification();
//        if(pojo==null
//                || spec==null) {
//            return;
//        }
//        val actualSpec = spec.getSpecificationLoader().specForType(pojo.getClass()).orElse(null);
//        if(!Objects.equals(spec,  actualSpec)) {
//            System.err.printf("spec mismatch %s %s%n", spec, actualSpec);
//        }
        //_Assert.assertEquals(spec, actualSpec);
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
        val canGetPojosWithoutSideeffect = !this.getSpecialization().getPojoPolicy().isRefetchable();
        if(canGetPojosWithoutSideeffect) {
            // expected to work for packed variant just fine, as it compares lists
            return Objects.equals(this.getPojo(), other.getPojo());
        }
        // objects are considered equal if their bookmarks match
        _Assert.assertTrue(other.isBookmarkMemoized()); // guarantee no side-effects on other
        return Objects.equals(
                sideEffectFreeBookmark(),
                other.getBookmark().orElseThrow(_Exceptions::unexpectedCodeReach));
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
                    : !getSpecialization().getBookmarkPolicy().isNoBookmark()
                        ? String.format("(refetchable, %s)", sideEffectFreeBookmark())
                        : "(refetchable, suppressed to not cause side effects)");
    }

    // -- HELPER

    private Bookmark sideEffectFreeBookmark() {
        _Assert.assertFalse(getSpecialization().getBookmarkPolicy().isNoBookmark());
        _Assert.assertTrue(isBookmarkMemoized());
        return getBookmark().orElseThrow(_Exceptions::unexpectedCodeReach);
    }


}
