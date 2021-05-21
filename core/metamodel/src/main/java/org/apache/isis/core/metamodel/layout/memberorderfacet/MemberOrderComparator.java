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

package org.apache.isis.core.metamodel.layout.memberorderfacet;

import java.util.Comparator;

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.members.layout.order.LayoutOrderFacet;
import org.apache.isis.core.metamodel.layout.DeweyOrderSet;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

import lombok.val;

/**
 * Compares by {@link ObjectMember}(s) obtained based on their {@link LayoutOrderFacet}.
 *
 * <p>
 * Will also compare {@link DeweyOrderSet}s; these are put after any
 * {@link FacetedMethod}s. If there is more than one DeweyOrderSet then these are
 * compared by an {@link OrderSetGroupNameComparator}.
 *
 * <p>
 * If there is no annotation on either member, then will compare the members by
 * name instead.
 *
 * <p>
 * Can specify if requires that members are in the same (group) name.
 */
public class MemberOrderComparator implements Comparator<Object> {

    private final Comparator<IdentifiedHolder> memberComparator;
    private final MemberIdentifierComparator memberIdentifierComparator = new MemberIdentifierComparator();
    private final OrderSetGroupNameComparator orderSetComparator = new OrderSetGroupNameComparator(true);

    public MemberOrderComparator(final boolean ensureGroupIsSame) {
        memberComparator = ObjectMember.Comparators.byMemberOrderSequence(ensureGroupIsSame);
    }

    @Override
    public int compare(final Object o1, final Object o2) {
        if (o1 instanceof IdentifiedHolder && o2 instanceof IdentifiedHolder) {
            val m1 = (IdentifiedHolder) o1;
            val m2 = (IdentifiedHolder) o2;
            final int memberOrderComparison = memberComparator.compare(m1, m2);
            if(memberOrderComparison != 0) {
                return memberOrderComparison;
            }
            return memberIdentifierComparator.compare(m1, m2);
        }
        if (o1 instanceof DeweyOrderSet && o2 instanceof DeweyOrderSet) {
            return orderSetComparator.compare((DeweyOrderSet) o1, (DeweyOrderSet) o2);
        }
        if (o1 instanceof IdentifiedHolder && o2 instanceof DeweyOrderSet) {
            return -1; // members before OrderSets.
        }
        if (o1 instanceof DeweyOrderSet && o2 instanceof IdentifiedHolder) {
            return +1; // members before OrderSets.
        }
        throw _Exceptions.illegalArgument(
                "can only compare IdentifiedHolders and DeweyOrderSets, got: %s, %s",
                o1==null ? null : o1.getClass().getName(),
                o2==null ? null : o2.getClass().getName());
    }

}
