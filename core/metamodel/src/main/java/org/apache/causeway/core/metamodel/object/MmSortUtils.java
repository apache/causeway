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

import java.util.Comparator;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.commons.internal.base._Objects;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.compare._Comparators;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.members.layout.group.LayoutGroupFacet;
import org.apache.causeway.core.metamodel.facets.members.layout.order.LayoutOrderFacet;
import org.apache.causeway.core.metamodel.layout.DeweyOrderSet;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MmSortUtils {

    public enum SortDirection {
        ASCENDING,
        DESCENDING;
    }

    public Comparator<ManagedObject> orderingBy(final ObjectAssociation sortProperty, final SortDirection sortDirection) {
        final Comparator<ManagedObject> comparator = (sortDirection != SortDirection.DESCENDING)
                ? NATURAL_NULL_FIRST
                : NATURAL_NULL_FIRST.reversed();

        return (p, q) -> {
            var pSort = sortProperty.get(p, InteractionInitiatedBy.PASS_THROUGH);
            var qSort = sortProperty.get(q, InteractionInitiatedBy.PASS_THROUGH);
            return comparator.compare(pSort, qSort);
        };
    }

    public int compare(final @Nullable ManagedObject p, final @Nullable ManagedObject q) {
        return NATURAL_NULL_FIRST.compare(p, q);
    }

    // -- PREDEFINED COMPARATOR

    final Comparator<ManagedObject> NATURAL_NULL_FIRST = (a, b) -> {
        var aPojo = MmUnwrapUtils.single(a);
        var bPojo = MmUnwrapUtils.single(b);
        if(Objects.equals(aPojo, bPojo)) return 0;
        if((aPojo==null
                || aPojo instanceof Comparable)
            && (bPojo==null
                    || bPojo instanceof Comparable)) {
            return _Objects.compareNullsFirst((Comparable<?>)aPojo, (Comparable<?>)bPojo);
        }
        final int hashCompare = Integer.compare(Objects.hashCode(aPojo), Objects.hashCode(bPojo));
        if(hashCompare!=0) return hashCompare;
        //XXX on hash-collision we return an arbitrary non-equal relation (unspecified behavior)
        return -1;
    };

    public int memberIdentifierCompare(final FacetHolder o1, final FacetHolder o2) {
        final Identifier identifier1 = o1.getFeatureIdentifier();
        final Identifier identifier2 = o2.getFeatureIdentifier();
        return identifier1.compareTo(identifier2);
    }

    /**
     * Compares by (simple) group name of each {@link DeweyOrderSet}.
     * <p>
     * Note that it only makes sense to use this comparator for {@link DeweyOrderSet}s
     * that are known to have the same parent {@link DeweyOrderSet}s.
     */
    public int compareDeweyOrderSet(final boolean assertInSameGroupPath, final DeweyOrderSet o1, final DeweyOrderSet o2) {
        if (assertInSameGroupPath
                && !o1.getGroupPath().equals(o2.getGroupPath())) {
            throw new IllegalArgumentException("OrderSets being compared do not have the same group path");
        }
        final String groupName1 = o1.getGroupName();
        final String groupName2 = o2.getGroupName();
        return groupName1.compareTo(groupName2);
    }

    /**
     * Compares by {@link ObjectMember}(s) obtained based on their {@link LayoutOrderFacet}.
     * <p>
     * Will also compare {@link DeweyOrderSet}s; these are put after any
     * {@link FacetedMethod}s. If there is more than one DeweyOrderSet then these are
     * compared by {@link #compareDeweyOrderSet(boolean, DeweyOrderSet, DeweyOrderSet)}.
     * <p>
     * If there is no annotation on either member, then will compare the members by
     * name instead.
     * <p>
     * Can specify if requires that members are in the same (group) name.
     */
    public int compareMemberOrder(final boolean assertInSameGroupPath, final Object o1, final Object o2) {
        if (o1 instanceof FacetHolder fh1
            && o2 instanceof FacetHolder fh2) {
            final int memberOrderComparison = compareMemberOrderSequence(assertInSameGroupPath, fh1, fh2);
            return memberOrderComparison != 0
                ? memberOrderComparison
                : memberIdentifierCompare(fh1, fh2);
        }
        if (o1 instanceof DeweyOrderSet dos1
            && o2 instanceof DeweyOrderSet dos2) {
            return compareDeweyOrderSet(assertInSameGroupPath, dos1, dos2);
        }
        if (o1 instanceof FacetHolder && o2 instanceof DeweyOrderSet) return -1; // members before OrderSets.
        if (o1 instanceof DeweyOrderSet && o2 instanceof FacetHolder) return +1; // members before OrderSets.

        throw _Exceptions.illegalArgument(
                "can only compare IdentifiedHolders and DeweyOrderSets, got: %s, %s",
                o1==null ? null : o1.getClass().getName(),
                o2==null ? null : o2.getClass().getName());
    }

    public int compareMemberOrderSequence(final boolean assertInSameGroup, final FacetHolder m1, final FacetHolder m2) {
        var orderFacet1 = m1==null ? null : m1.getFacet(LayoutOrderFacet.class);
        var orderFacet2 = m2==null ? null : m2.getFacet(LayoutOrderFacet.class);

        if (orderFacet1 == null && orderFacet2 == null) return 0;
        if (orderFacet1 == null && orderFacet2 != null) return +1; // annotated before non-annotated
        if (orderFacet1 != null && orderFacet2 == null) return -1; // annotated before non-annotated

        if(assertInSameGroup) {
            var groupFacet1 = m1.getFacet(LayoutGroupFacet.class);
            var groupFacet2 = m2.getFacet(LayoutGroupFacet.class);
            var groupId1 = _Strings.nullToEmpty(groupFacet1==null ? null : groupFacet1.getGroupId());
            var groupId2 = _Strings.nullToEmpty(groupFacet2==null ? null : groupFacet2.getGroupId());

            if(!Objects.equals(groupId1, groupId2)) {
                throw _Exceptions.illegalArgument(
                        "Not in same fieldSetId1 when comparing: '%s', '%s'",
                        groupId1,
                        groupId2);
            }
        }

        return _Comparators.deweyOrderCompare(
                orderFacet1.getSequence(),
                orderFacet2.getSequence());
    }

}
