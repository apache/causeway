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
import java.util.StringTokenizer;

import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;

public class MemberOrderFacetComparator implements Comparator<MemberOrderFacet> {

    private boolean ensureInSameGroup;
    public MemberOrderFacetComparator(boolean ensureInSameGroup) {
        this.ensureInSameGroup = ensureInSameGroup;
    }
    
    @Override
    public int compare(final MemberOrderFacet m1, final MemberOrderFacet m2) {
        if (m1 == null && m2 == null) {
            return 0;
        }

        if (m1 == null && m2 != null) {
            return +1; // annotated before non-annotated
        }
        if (m1 != null && m2 == null) {
            return -1; // annotated before non-annotated
        }

        if (ensureInSameGroup && !m1.name().equals(m2.name())) {
            throw new IllegalArgumentException("Not in same group");
        }

        final String sequence1 = m1.sequence();
        final String sequence2 = m2.sequence();

        final String[] components1 = componentsFor(sequence1);
        final String[] components2 = componentsFor(sequence2);

        final int length1 = components1.length;
        final int length2 = components2.length;

        // shouldn't happen but just in case.
        if (length1 == 0 && length2 == 0) {
            return 0;
        }

        // continue to loop until we run out of components.
        int n = 0;
        while (true) {
            final int length = n + 1;
            // check if run out of components in either side
            if (length1 < length && length2 >= length) {
                return -1; // o1 before o2
            }
            if (length2 < length && length1 >= length) {
                return +1; // o2 before o1
            }
            if (length1 < length && length2 < length) {
                // run out of components
                return 0;
            }
            // we have this component on each side

            int componentCompare = 0;
            try {
                final Integer c1 = Integer.valueOf(components1[n]);
                final Integer c2 = Integer.valueOf(components2[n]);
                componentCompare = c1.compareTo(c2);
            } catch (final NumberFormatException nfe) {
                // not integers compare as strings
                componentCompare = components1[n].compareTo(components2[n]);
            }

            if (componentCompare != 0) {
                return componentCompare;
            }
            // this component is the same; lets look at the next
            n++;
        }
    }

    private static String[] componentsFor(final String sequence) {
        final StringTokenizer tokens = new StringTokenizer(sequence, ".", false);
        final String[] components = new String[tokens.countTokens()];
        for (int i = 0; tokens.hasMoreTokens(); i++) {
            components[i] = tokens.nextToken();
        }
        return components;
    }


}
