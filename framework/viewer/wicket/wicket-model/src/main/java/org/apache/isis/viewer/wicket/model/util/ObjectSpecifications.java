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

package org.apache.isis.viewer.wicket.model.util;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import org.apache.isis.core.metamodel.facets.object.membergroups.MemberGroupsFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;


public final class ObjectSpecifications {

    private ObjectSpecifications() {
    }

    public static List<String> orderByMemberGroups(ObjectSpecification objSpec, Set<String> groupNamesToOrder) {
        final MemberGroupsFacet facet = objSpec.getFacet(MemberGroupsFacet.class);
        final List<String> groupNames = Lists.newArrayList(groupNamesToOrder);
        
        // not expected to happen
        if(facet == null) {
            return groupNames;
        }
        
        final List<String> groupNamedInRequiredOrder = facet.value();
        return order(groupNames, groupNamedInRequiredOrder);
    }

    static List<String> order(final List<String> valuesToOrder, final List<String> valuesInRequiredOrder) {
        int i=0;
        for(String memberGroup: valuesInRequiredOrder) {
            if(valuesToOrder.contains(memberGroup)) {
                // move to next position
                valuesToOrder.remove(memberGroup);
                valuesToOrder.add(i++, memberGroup);
            }
        }
        
        return valuesToOrder;
    }


}
