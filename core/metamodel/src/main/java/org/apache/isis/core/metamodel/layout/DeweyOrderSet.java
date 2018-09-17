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

package org.apache.isis.core.metamodel.layout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.layout.memberorderfacet.MemberIdentifierComparator;
import org.apache.isis.core.metamodel.layout.memberorderfacet.MemberOrderComparator;

/**
 * Represents a nested hierarchy of ordered members.
 *
 * <p>
 * At each level the elements are either {@link FacetedMethod}s or they are
 * instances of {@link OrderSet} represent a group of {@link FacetedMethod}s
 * that have a {@link MemberOrderFacet} of the same name.
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
public class DeweyOrderSet implements Comparable<DeweyOrderSet>, Iterable<Object>  {

    public static DeweyOrderSet createOrderSet(final List<? extends IdentifiedHolder> identifiedHolders) {

        final SortedMap<String, SortedSet<IdentifiedHolder>> sortedMembersByGroup = _Maps.newTreeMap();
        final SortedSet<IdentifiedHolder> nonAnnotatedGroup = _Sets.newTreeSet(new MemberIdentifierComparator());

        // spin over all the members and put them into a Map of SortedSets
        // any non-annotated members go into additional nonAnnotatedGroup set.
        for (final IdentifiedHolder identifiedHolder : identifiedHolders) {
            final MemberOrderFacet memberOrder = identifiedHolder.getFacet(MemberOrderFacet.class);
            if (memberOrder == null) {
                nonAnnotatedGroup.add(identifiedHolder);
                continue;
            }
            final SortedSet<IdentifiedHolder> sortedMembersForGroup = getSortedSet(sortedMembersByGroup, memberOrder.name());
            sortedMembersForGroup.add(identifiedHolder);
        }

        // add the non-annotated group to the first "" group.
        final SortedSet<IdentifiedHolder> defaultSet = getSortedSet(sortedMembersByGroup, "");
        defaultSet.addAll(nonAnnotatedGroup);

        // create OrderSets, wiring up parents and children.

        // since sortedMembersByGroup is a SortedMap, the
        // iteration will be in alphabetical order (ie parent groups before
        // their children).
        final Set<String> groupNames = sortedMembersByGroup.keySet();
        final SortedMap<String, DeweyOrderSet> orderSetsByGroup = _Maps.newTreeMap();

        for (final String string : groupNames) {
            final String groupName = string;
            final DeweyOrderSet deweyOrderSet = new DeweyOrderSet(groupName);
            orderSetsByGroup.put(groupName, deweyOrderSet);
            ensureParentFor(orderSetsByGroup, deweyOrderSet);
        }

        // now populate the OrderSets
        for (final String groupName : groupNames) {
            final DeweyOrderSet deweyOrderSet = orderSetsByGroup.get(groupName);
            // REVIEW: something fishy happens here with casting, hence warnings
            // left in
            final SortedSet sortedMembers = sortedMembersByGroup.get(groupName);
            deweyOrderSet.addAll(sortedMembers);
            deweyOrderSet.copyOverChildren();
        }

        return orderSetsByGroup.get("");
    }

    /**
     * Recursively creates parents all the way up to root (<tt>""</tt>), along
     * the way associating each child with its parent and adding the child as an
     * element of its parent.
     *
     * @param orderSetsByGroup
     * @param deweyOrderSet
     */
    private static void ensureParentFor(final SortedMap<String,DeweyOrderSet> orderSetsByGroup, final DeweyOrderSet deweyOrderSet) {
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
     * Gets the SortedSet with the specified group from the supplied Map of
     * SortedSets.
     *
     * <p>
     * If there is no such SortedSet, creates.
     *
     * @param sortedMembersByGroup
     * @param groupName
     * @return
     */
    private static SortedSet<IdentifiedHolder> getSortedSet(final SortedMap<String, SortedSet<IdentifiedHolder>> sortedMembersByGroup, final String groupName) {
        SortedSet<IdentifiedHolder> sortedMembersForGroup = sortedMembersByGroup.get(groupName);
        if (sortedMembersForGroup == null) {
            sortedMembersForGroup = new TreeSet<IdentifiedHolder>(new MemberOrderComparator(true));
            sortedMembersByGroup.put(groupName, sortedMembersForGroup);
        }
        return sortedMembersForGroup;
    }

    // /////////////////////////////////////////////////////////////////////////

    private final List<Object> elements = _Lists.newArrayList();
    private final String groupFullName;
    private final String groupName;
    private final String groupPath;

    private DeweyOrderSet parent;

    /**
     * A staging area until we are ready to add the child sets to the collection
     * of elements owned by the superclass.
     */
    protected SortedSet<DeweyOrderSet> childOrderSets = new TreeSet<DeweyOrderSet>();

    // /////////////////////////////////////////////////////////////////////////

    private DeweyOrderSet(final String groupFullName) {
        this.groupFullName = groupFullName;

        groupName = deriveGroupName(groupFullName);
        groupPath = deriveGroupPath(groupFullName);
    }

    // ///////////////// Group Name etc ////////////////////

    /**
     * Last component of the comma-separated group name supplied in the
     * constructor (analogous to the file name extracted from a fully qualified
     * file name).
     *
     * <p>
     * For example, if supplied <tt>abc,def,ghi</tt> in the constructor, then
     * this will return <tt>ghi</tt>.
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * The group name exactly as it was supplied in the constructor (analogous
     * to a fully qualified file name).
     *
     * <p>
     * For example, if supplied <tt>abc,def,ghi</tt> in the constructor, then
     * this will return the same string <tt>abc,def,ghi</tt>.
     */
    public String getGroupFullName() {
        return groupFullName;
    }

    /**
     * Represents the parent groups, derived from the group name supplied in the
     * constructor (analogous to the directory portion of a fully qualified file
     * name).
     *
     * <p>
     * For example, if supplied <tt>abc,def,ghi</tt> in the constructor, then
     * this will return <tt>abc,def</tt>.
     */
    public String getGroupPath() {
        return groupPath;
    }

    /**
     * Splits name by comma, then title case the last component.
     */
    private static String deriveGroupName(final String groupFullName) {
        final StringTokenizer tokens = new StringTokenizer(groupFullName, ",", false);
        final String[] groupNameComponents = new String[tokens.countTokens()];
        for (int i = 0; tokens.hasMoreTokens(); i++) {
            groupNameComponents[i] = tokens.nextToken();
        }
        final String groupSimpleName = groupNameComponents.length > 0 ? groupNameComponents[groupNameComponents.length - 1] : "";
        if (groupSimpleName.length() > 1) {
            return groupSimpleName.substring(0, 1).toUpperCase() + groupSimpleName.substring(1);
        } else {
            return groupSimpleName.toUpperCase();
        }

    }

    /**
     * Everything up to the last comma, else empty string if none.
     */
    private static String deriveGroupPath(final String groupFullName) {
        final int lastComma = groupFullName.lastIndexOf(",");
        if (lastComma == -1) {
            return "";
        }
        return groupFullName.substring(0, lastComma);
    }

    // ///////////////////// Parent & Children ///////////////////

    protected void setParent(final DeweyOrderSet parent) {
        this.parent = parent;
    }

    public DeweyOrderSet getParent() {
        return parent;
    }

    protected void addChild(final DeweyOrderSet childOrderSet) {
        childOrderSets.add(childOrderSet);
    }

    public List<DeweyOrderSet> children() {
        final ArrayList<DeweyOrderSet> list = new ArrayList<DeweyOrderSet>();
        list.addAll(childOrderSets);
        return list;
    }

    protected void copyOverChildren() {
        addAll(childOrderSets);
    }

    // ///////////////////// Elements (includes children) ///////////////////

    /**
     * Returns a copy of the elements, in sequence.
     */
    public List<Object> elementList() {
        return new ArrayList<Object>(elements);
    }

    public int size() {
        return elements.size();
    }

    protected void addElement(final Object element) {
        elements.add(element);
    }

    @Override
    public Iterator<Object> iterator() {
        return elements.iterator();
    }

    protected void addAll(final SortedSet<?> sortedMembers) {
        for (final Object deweyOrderSet : sortedMembers) {
            this.addElement(deweyOrderSet);
        }
    }

    // ///////////////////////// reorderChildren //////////////////////

    public void reorderChildren(List<String> requiredOrder) {
        final LinkedHashMap<String,DeweyOrderSet> orderSets = _Maps.newLinkedHashMap();

        // remove all OrderSets from elements
        // though remembering the order they were encountered
        for (Object child : elementList()) {
            if(child instanceof DeweyOrderSet) {
                final DeweyOrderSet orderSet = (DeweyOrderSet) child;
                elements.remove(orderSet);
                orderSets.put(orderSet.getGroupName(), orderSet);
            }
        }

        // spin through the requiredOrder and add back in (if found)
        for (String group : requiredOrder) {
            DeweyOrderSet orderSet = orderSets.get(group);
            if(orderSet == null) {
                continue;
            }
            orderSets.remove(group);
            elements.add(orderSet);
        }

        // anything left, add back in the original order
        for (String orderSetGroupName : orderSets.keySet()) {
            final DeweyOrderSet orderSet = orderSets.get(orderSetGroupName);
            elements.add(orderSet);
        }
    }


    // //////////////////////////////////////
    // compareTo, equals, hashCode, toString
    // //////////////////////////////////////

    /**
     * Natural ordering is to compare by {@link #getGroupFullName()}.
     */
    @Override
    public int compareTo(final DeweyOrderSet o) {
        if (this.equals(o)) {
            return 0;
        }
        return groupFullName.compareTo(o.groupFullName);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DeweyOrderSet other = (DeweyOrderSet) obj;
        if (groupFullName == null) {
            if (other.groupFullName != null) {
                return false;
            }
        } else if (!groupFullName.equals(other.groupFullName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((groupFullName == null) ? 0 : groupFullName.hashCode());
        return result;
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
