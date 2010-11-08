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


package org.apache.isis.core.progmodel.facets.ordering;

import java.util.StringTokenizer;

import org.apache.isis.core.metamodel.specloader.internal.peer.ObjectMemberPeer;
import org.apache.isis.core.metamodel.util.NameUtils;


public class SimpleOrderSet extends OrderSet {
    public static SimpleOrderSet createOrderSet(final String order, final ObjectMemberPeer[] members) {
        SimpleOrderSet set = new SimpleOrderSet(members);

        final StringTokenizer st = new StringTokenizer(order, ",");
        while (st.hasMoreTokens()) {
            String element = st.nextToken().trim();

            boolean ends;
            if (ends = element.endsWith(")")) {
                element = element.substring(0, element.length() - 1).trim();
            }

            if (element.startsWith("(")) {
                final int colon = element.indexOf(':');
                final String groupName = element.substring(1, colon).trim();
                element = element.substring(colon + 1).trim();
                set = set.createSubOrderSet(groupName, element);
            } else {
                set.add(element);
            }

            if (ends) {
                set = set.parent;
            }
        }
        set.addAnyRemainingMember();
        return set;
    }

    private final SimpleOrderSet parent;
    private final ObjectMemberPeer[] members;

    private SimpleOrderSet(final ObjectMemberPeer[] members) {
        super("");
        this.members = members;
        parent = null;
    }

    private SimpleOrderSet(
            final SimpleOrderSet set,
            final String groupName,
            final String name,
            final ObjectMemberPeer[] members) {
        super(groupName);
        parent = set;
        parent.addElement(this);
        this.members = members;
        add(name);
    }

    private void add(final String name) {
        final ObjectMemberPeer memberWithName = getMemberWithName(name);
        if (memberWithName != null) {
            addElement(memberWithName);
        }
    }

    private void addAnyRemainingMember() {
        for (int i = 0; i < members.length; i++) {
            if (members[i] != null) {
                addElement(members[i]);
            }
        }

    }

    private SimpleOrderSet createSubOrderSet(final String groupName, final String memberName) {
        return new SimpleOrderSet(this, groupName, memberName, members);
    }

    private ObjectMemberPeer getMemberWithName(final String name) {
        final String searchName = NameUtils.simpleName(name);
        for (int i = 0; i < members.length; i++) {
            final ObjectMemberPeer member = members[i];
            if (member != null) {
                final String testName = NameUtils.simpleName(member.getIdentifier().getMemberName());
                if (testName.equals(searchName)) {
                    members[i] = null;
                    return member;
                }
            }
        }
        return null;
    }

}

