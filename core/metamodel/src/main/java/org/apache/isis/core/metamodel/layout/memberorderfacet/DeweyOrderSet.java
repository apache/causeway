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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.ordering.MemberOrderFacet;
import org.apache.isis.core.metamodel.layout.OrderSet;


/**
 * Represents a nested hierarchy of ordered members.
 * 
 * <p>
 * At each level the elements are either {@link FacetedMethod}s or they are instances of
 * {@link OrderSet} represent a group of {@link FacetedMethod}s that have a {@link MemberOrderFacet}
 * of the same name.
 * 
 * <p>
 * With no name, (ie <tt>name=""</tt> is the default), at the top level
 * 
 * <pre>
 * MemberOrder(sequence=&quot;1&quot;)
 * MemberOrder(sequence=&quot;1.1&quot;)
 * MemberOrder(sequence=&quot;1.2&quot;)
 * MemberOrder(sequence=&quot;1.2.1&quot;)
 * MemberOrder(sequence=&quot;1.3&quot;)
 * </pre>
 * 
 * <p>
 * With names, creates a hierarchy.
 * 
 * <pre>
 * MemberOrder(sequence=&quot;1.1&quot;)                   // no parent
 * MemberOrder(sequence=&quot;1.2.1&quot;)
 * MemberOrder(sequence=&quot;1.3&quot;)
 * MemberOrder(name=&quot;abc&quot;, sequence=&quot;1&quot;)         // group is abc, parent is &quot;&quot;
 * MemberOrder(name=&quot;abc&quot;, sequence=&quot;1.2&quot;)
 * MemberOrder(name=&quot;abc,def&quot;, sequence=&quot;1&quot;)     // group is def, parent is abc
 * MemberOrder(name=&quot;abc,def&quot;, sequence=&quot;1.2&quot;)
 * </pre>
 * 
 */
public class DeweyOrderSet extends OrderSet {
    public static DeweyOrderSet createOrderSet(final List<FacetedMethod> members) {

        final SortedMap sortedMembersByGroup = new TreeMap();
        final SortedSet nonAnnotatedGroup = new TreeSet(new MemberIdentifierComparator());

        // spin over all the members and put them into a Map of SortedSets
        // any non-annotated members go into additional nonAnnotatedGroup set.
        for (FacetedMethod member : members) {
            final MemberOrderFacet memberOrder = member.getFacet(MemberOrderFacet.class);
            if (memberOrder == null) {
                nonAnnotatedGroup.add(member);
                continue;
            }
            final SortedSet sortedMembersForGroup = getSortedSet(sortedMembersByGroup, memberOrder.name());
            sortedMembersForGroup.add(member);
        }

        // add the non-annotated group to the first "" group.
        final SortedSet defaultSet = getSortedSet(sortedMembersByGroup, "");
        defaultSet.addAll(nonAnnotatedGroup);

        // create OrderSets, wiring up parents and children.

        // since sortedMembersByGroup is a SortedMap, the
        // iteration will be in alphabetical order (ie parent groups before their children).
        final Set groupNames = sortedMembersByGroup.keySet();
        final SortedMap orderSetsByGroup = new TreeMap();

        for (final Iterator iter = groupNames.iterator(); iter.hasNext();) {
            final String groupName = (String) iter.next();
            final DeweyOrderSet deweyOrderSet = new DeweyOrderSet(groupName);
            orderSetsByGroup.put(groupName, deweyOrderSet);
            ensureParentFor(orderSetsByGroup, deweyOrderSet);
        }

        // now populate the OrderSets
        for (final Iterator iter = groupNames.iterator(); iter.hasNext();) {
            final String groupName = (String) iter.next();
            final DeweyOrderSet deweyOrderSet = (DeweyOrderSet) orderSetsByGroup.get(groupName);
            final SortedSet sortedMembers = (SortedSet) sortedMembersByGroup.get(groupName);
            deweyOrderSet.addAll(sortedMembers);
            deweyOrderSet.copyOverChildren();
        }

        return (DeweyOrderSet) orderSetsByGroup.get("");
    }

    /**
     * Recursively creates parents all the way up to root (<tt>""</tt>), along the way associating each child
     * with its parent and adding the child as an element of its parent.
     * 
     * @param orderSetsByGroup
     * @param deweyOrderSet
     */
    private static void ensureParentFor(final SortedMap orderSetsByGroup, final DeweyOrderSet deweyOrderSet) {
        final String parentGroup = deweyOrderSet.getGroupPath();
        DeweyOrderSet parentOrderSet = (DeweyOrderSet) orderSetsByGroup.get(parentGroup);
        if (parentOrderSet == null) {
            parentOrderSet = new DeweyOrderSet(parentGroup);
            orderSetsByGroup.put(parentGroup, parentOrderSet);
            if (!parentGroup.equals("")) {
                ensureParentFor(orderSetsByGroup, deweyOrderSet);
            }
        }
        // check in case at root
        if (deweyOrderSet != parentOrderSet) {
            deweyOrderSet.setParent(parentOrderSet);
            parentOrderSet.addChild(deweyOrderSet);
        }
    }

    /**
     * Gets the SortedSet with the specified group from the supplied Map of SortedSets.
     * 
     * <p>
     * If there is no such SortedSet, creates.
     * 
     * @param sortedMembersByGroup
     * @param groupName
     * @return
     */
    private static SortedSet getSortedSet(final SortedMap sortedMembersByGroup, final String groupName) {
        SortedSet sortedMembersForGroup;
        sortedMembersForGroup = (SortedSet) sortedMembersByGroup.get(groupName);
        if (sortedMembersForGroup == null) {
            sortedMembersForGroup = new TreeSet(new MemberOrderComparator(true));
            sortedMembersByGroup.put(groupName, sortedMembersForGroup);
        }
        return sortedMembersForGroup;
    }

    // /////////////////////////////////////////////////////////////////////////

    private DeweyOrderSet(final String groupName) {
        super(groupName);
    }

    /**
     * Format is: <tt>abc,def:XXel/YYm/ZZch</tt>
     * <p>
     * where <tt>abc,def</tt> is group name, <tt>XX</tt> is number of elements,
     * <tt>YY is number of members, and 
     * <tt>ZZ</tt> is number of child order sets.
     */
    @Override
    public String toString() {
        return getGroupFullName() + ":" + size() + "el/" + (size() - childOrderSets.size()) + "m/" + childOrderSets.size() + "ch";
    }

}

