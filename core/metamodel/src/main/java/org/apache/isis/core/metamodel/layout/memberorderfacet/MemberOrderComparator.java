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

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.layout.DeweyOrderSet;

/**
 * Compares by {@link MemberOrderFacet} obtained from each {@link FacetedMethod}
 * ).
 *
 * <p>
 * Will also compare {@link OrderSet}s; these are put after any
 * {@link FacetedMethod}s. If there is more than one OrderSet then these are
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

    private final MemberOrderFacetComparator memberOrderFacetComparator;
    private final MemberIdentifierComparator memberIdentifierComparator = new MemberIdentifierComparator();
    private final OrderSetGroupNameComparator orderSetComparator = new OrderSetGroupNameComparator(true);

    public MemberOrderComparator(final boolean ensureGroupIsSame) {
        memberOrderFacetComparator = new MemberOrderFacetComparator(ensureGroupIsSame);
    }


    @Override
    public int compare(final Object o1, final Object o2) {
        if (o1 instanceof IdentifiedHolder && o2 instanceof IdentifiedHolder) {
            return compare((IdentifiedHolder) o1, (IdentifiedHolder) o2);
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
        throw new IllegalArgumentException("can only compare IdentifiedHolders and DeweyOrderSets");
    }

    public int compare(final IdentifiedHolder o1, final IdentifiedHolder o2) {
        final MemberOrderFacet m1 = getMemberOrder(o1);
        final MemberOrderFacet m2 = getMemberOrder(o2);

        final int memberOrderComparison = memberOrderFacetComparator.compare(m1, m2);
        if(memberOrderComparison != 0) {
            return memberOrderComparison;
        }
        return memberIdentifierComparator.compare(o1, o2);
    }


    private MemberOrderFacet getMemberOrder(final FacetHolder facetHolder) {
        return facetHolder.getFacet(MemberOrderFacet.class);
    }

}
