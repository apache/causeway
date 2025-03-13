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

import java.util.Objects;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.experimental.UtilityClass;

@UtilityClass
class _Compliance {

    <T> T assertCompliance(
        final ObjectSpecification objSpec,
        final ManagedObject.Specialization specialization,
        final @NonNull T pojo) {

        MmAssertionUtils.assertPojoNotWrapped(pojo);
        if(objSpec.isAbstract()) {
            _Assert.assertFalse(specialization.getTypePolicy().isExactTypeRequired(),
                    ()->String.format("Specialization %s does not allow abstract type %s",
                            specialization,
                            objSpec));
        }
        if(specialization.getTypePolicy().isExactTypeRequired()) {
            MmAssertionUtils.assertExactType(objSpec, pojo);
        }
        if(specialization.getInjectionPolicy().isAlwaysInject()) {
            var isInjectionPointsResolved = objSpec.entityFacet()
                .map(entityFacet->entityFacet.isInjectionPointsResolved(pojo))
                .orElse(false);
            if(!isInjectionPointsResolved) {
                objSpec.getServiceInjector().injectServicesInto(pojo); // might be redundant
            }
        }
        return pojo;
    }

    boolean equals(final ManagedObject mo, final Object obj) {
        if(mo == obj) return true;
        if(!(obj instanceof ManagedObject)) return false;

        var other = (ManagedObject)obj;
        if(!mo.specialization().equals(other.specialization())) return false;
        if(!mo.objSpec().equals(other.objSpec())) return false;

        if(!mo.specialization().getPojoPolicy().isRefetchable()) {
            // expected to work for packed variant just fine, as it compares lists
            return Objects.equals(mo.getPojo(), other.getPojo());
        }

        if(mo.isBookmarkMemoized()
                && other.isBookmarkMemoized()) {
            return Objects.equals(
                    sideEffectFreeBookmark(mo),
                    other.getBookmark().orElseThrow(_Exceptions::unexpectedCodeReach));
        }

        var a = (ManagedObjectEntity) mo;
        var b = (ManagedObjectEntity) other;
        return Objects.equals(a.peekAtPojo(), b.peekAtPojo());
    }

    int hashCode(final ManagedObject mo) {
        // make sure hashCode() is without side-effects!
        var canGetPojosWithoutSideeffect = !mo.specialization().getPojoPolicy().isRefetchable();
        return canGetPojosWithoutSideeffect
                // expected to work for packed variant just fine, as it compares lists
                ? Objects.hash(mo.objSpec().getCorrespondingClass(), mo.getPojo())
                : Objects.hash(mo.objSpec().getCorrespondingClass(), sideEffectFreeBookmark(mo));
    }

    String toString(final ManagedObject mo) {
        // make sure toString() is without side-effects!
        return String.format("ManagedObject(%s, spec=%s, pojo=%s)",
            mo.specialization().name(),
            mo.objSpec(),
            !mo.specialization().getPojoPolicy().isRefetchable()
                ? mo.getPojo() // its safe to get pojo side-effect free
                : mo.isBookmarkMemoized()
                    ? String.format("(refetchable, %s)", sideEffectFreeBookmark(mo))
                    : "(refetchable, suppressed to not cause side effects)");
    }

    private Bookmark sideEffectFreeBookmark(final ManagedObject mo) {
        _Assert.assertTrue(mo.isBookmarkMemoized());
        return mo.getBookmark().orElseThrow(_Exceptions::unexpectedCodeReach);
    }

}
