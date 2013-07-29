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

package org.apache.isis.core.metamodel.spec;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.MemberGroupLayout.ColumnSpans;
import org.apache.isis.core.metamodel.facets.object.membergroups.MemberGroupLayoutFacet;


public final class ObjectSpecifications {

    private ObjectSpecifications() {
    }

    public enum MemberGroupLayoutHint {
        LEFT,
        MIDDLE,
        RIGHT;

        public int from(ColumnSpans columnSpans) {
            if(this == LEFT) return columnSpans.getLeft();
            if(this == MIDDLE) return columnSpans.getMiddle();
            if(this == RIGHT) return columnSpans.getRight();
            return 0;
        }
    }

    public static List<String> orderByMemberGroups(ObjectSpecification objSpec, Set<String> groupNamesToOrder, MemberGroupLayoutHint memberGroupLayoutHint) {
        final MemberGroupLayoutFacet facet = objSpec.getFacet(MemberGroupLayoutFacet.class);
        final List<String> leftColumnGroupNames = Lists.newArrayList(groupNamesToOrder);
        
        // not expected to happen
        if(facet == null) {
            return leftColumnGroupNames;
        }
        
        if(memberGroupLayoutHint == MemberGroupLayoutHint.MIDDLE) {
            return facet.getColumnSpans().getMiddle()>0? facet.getMiddle(): Collections.<String>emptyList();
        }
        if(memberGroupLayoutHint == MemberGroupLayoutHint.RIGHT) {
            return facet.getColumnSpans().getRight()>0? facet.getRight(): Collections.<String>emptyList();
        }
        
        // else left; per the requested order, including any groups not mentioned in either list, 
        // but excluding any groups in the middle or right columns
        final List<String> groupNamedInRequiredOrder = facet.getLeft();
        final List<String> order = order(leftColumnGroupNames, groupNamedInRequiredOrder);
        
        if(facet.getColumnSpans().getMiddle() > 0) {
            order.removeAll(facet.getMiddle());
        }
        if(facet.getColumnSpans().getRight() > 0) {
            order.removeAll(facet.getRight());
        }
        return order;
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
