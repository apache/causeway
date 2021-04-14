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

import java.io.Serializable;
import java.util.Comparator;

import org.apache.isis.core.metamodel.layout.DeweyOrderSet;

/**
 * Compares by (simple) group name of each {@link DeweyOrderSet}.
 *
 * <p>
 * Note that it only makes sense to use this comparator for {@link DeweyOrderSet}s
 * that are known to have the same parent {@link DeweyOrderSet}s.
 */
public class OrderSetGroupNameComparator implements Comparator<DeweyOrderSet>, Serializable {

    private static final long serialVersionUID = 1L;

    public OrderSetGroupNameComparator(final boolean ensureInSameGroupPath) {
        this.ensureInSameGroupPath = ensureInSameGroupPath;
    }

    private final boolean ensureInSameGroupPath;

    @Override
    public int compare(final DeweyOrderSet o1, final DeweyOrderSet o2) {
        if (ensureInSameGroupPath && !o1.getGroupPath().equals(o2.getGroupPath())) {
            throw new IllegalArgumentException("OrderSets being compared do not have the same group path");
        }

        final String groupName1 = o1.getGroupName();
        final String groupName2 = o2.getGroupName();

        return groupName1.compareTo(groupName2);
    }
}
