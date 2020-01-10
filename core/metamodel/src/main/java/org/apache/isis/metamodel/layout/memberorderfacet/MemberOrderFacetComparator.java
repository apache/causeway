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

package org.apache.isis.metamodel.layout.memberorderfacet;

import java.util.Comparator;

import org.apache.isis.core.commons.internal.compare._Comparators;
import org.apache.isis.metamodel.facets.members.order.MemberOrderFacet;

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

        return _Comparators.deweyOrderCompare(m1.sequence(), m2.sequence());

    }


}
