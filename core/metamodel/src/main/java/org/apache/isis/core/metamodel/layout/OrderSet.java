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
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import com.google.common.collect.Lists;

import org.apache.isis.core.metamodel.layout.memberorderfacet.DeweyOrderSet;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

/**
 * This represents a particular layout of {@link ObjectMember}s.
 * 
 * <p>
 * Currently it only supports the concept of ordering and nesting.
 */
public class OrderSet implements Comparable<OrderSet>, Iterable<Object> {

    private final List<Object> elements = Lists.newArrayList();
    private final String groupFullName;
    private final String groupName;
    private final String groupPath;

    private OrderSet parent;

    /**
     * A staging area until we are ready to add the child sets to the collection
     * of elements owned by the superclass.
     */
    protected SortedSet<DeweyOrderSet> childOrderSets = new TreeSet<DeweyOrderSet>();

    public OrderSet(final String groupFullName) {
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
     * 
     * @return
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
     * 
     * @return
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
     * 
     * @return
     */
    public String getGroupPath() {
        return groupPath;
    }

    /**
     * Splits name by comma, then title case the last component.
     * 
     * @param groupFullName
     * @return
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
     * Everything upto the last comma, else empty string if none.
     * 
     * @param groupFullName
     * @return
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

    public OrderSet getParent() {
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
     * 
     * @return
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

    // ///////////////////////// compareTo //////////////////////

    /**
     * Natural ordering is to compare by {@link #getGroupFullName()}.
     */
    @Override
    public int compareTo(final OrderSet o) {
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
        final OrderSet other = (OrderSet) obj;
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

}
