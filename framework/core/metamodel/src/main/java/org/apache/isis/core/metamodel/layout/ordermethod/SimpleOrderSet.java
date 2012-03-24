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

package org.apache.isis.core.metamodel.layout.ordermethod;

import java.util.List;
import java.util.StringTokenizer;

import org.apache.isis.core.commons.lang.NameUtils;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.layout.OrderSet;

public class SimpleOrderSet extends OrderSet {
    public static SimpleOrderSet createOrderSet(final String order, final List<FacetedMethod> members) {
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
    private final List<FacetedMethod> members;

    private SimpleOrderSet(final List<FacetedMethod> members) {
        super("");
        this.members = members;
        parent = null;
    }

    private SimpleOrderSet(final SimpleOrderSet set, final String groupName, final String name, final List<FacetedMethod> members) {
        super(groupName);
        parent = set;
        parent.addElement(this);
        this.members = members;
        add(name);
    }

    private void add(final String name) {
        final FacetedMethod memberWithName = getMemberWithName(name);
        if (memberWithName != null) {
            addElement(memberWithName);
        }
    }

    private void addAnyRemainingMember() {
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i) != null) {
                final FacetedMethod member = members.get(i);
                addElement(member);
            }
        }

    }

    private SimpleOrderSet createSubOrderSet(final String groupName, final String memberName) {
        return new SimpleOrderSet(this, groupName, memberName, members);
    }

    private FacetedMethod getMemberWithName(final String name) {
        final String searchName = NameUtils.simpleName(name);
        for (int i = 0; i < members.size(); i++) {
            final FacetedMethod member = members.get(i);
            if (member != null) {
                final String testName = NameUtils.simpleName(member.getIdentifier().getMemberName());
                if (testName.equals(searchName)) {
                    members.set(i, null);
                    return member;
                }
            }
        }
        return null;
    }

}
