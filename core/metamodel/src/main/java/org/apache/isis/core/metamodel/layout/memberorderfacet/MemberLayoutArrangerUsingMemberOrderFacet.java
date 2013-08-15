/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.layout.memberorderfacet;

import java.util.List;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.object.membergroups.MemberGroupLayoutFacet;
import org.apache.isis.core.metamodel.layout.MemberLayoutArranger;
import org.apache.isis.core.metamodel.layout.OrderSet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class MemberLayoutArrangerUsingMemberOrderFacet implements MemberLayoutArranger {

    private static final Logger LOG = LoggerFactory.getLogger(MemberLayoutArrangerUsingMemberOrderFacet.class);

    // ////////////////////////////////////////////////////////////////////////////
    // constructor
    // ////////////////////////////////////////////////////////////////////////////

    public MemberLayoutArrangerUsingMemberOrderFacet() {
    }

    // ////////////////////////////////////////////////////////////////////////////
    // associations
    // ////////////////////////////////////////////////////////////////////////////

    @Override
    public OrderSet createAssociationOrderSetFor(final ObjectSpecification spec, final List<FacetedMethod> associationMethods) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("MemberLayoutArrangerUsingMemberOrderFacet: createAssociationOrderSetFor " + spec.getFullIdentifier());
        }

        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(associationMethods);
        final MemberGroupLayoutFacet memberGroupLayoutFacet = spec.getFacet(MemberGroupLayoutFacet.class);
        
        if(memberGroupLayoutFacet != null) {
            final List<String> groupOrder = Lists.newArrayList();
            groupOrder.addAll(memberGroupLayoutFacet.getLeft());
            groupOrder.addAll(memberGroupLayoutFacet.getMiddle());
            groupOrder.addAll(memberGroupLayoutFacet.getRight());
            
            orderSet.reorderChildren(groupOrder);
        }
        return orderSet;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // actions
    // ////////////////////////////////////////////////////////////////////////////

    @Override
    public OrderSet createActionOrderSetFor(final ObjectSpecification spec, final List<FacetedMethod> actionFacetedMethodList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("MemberLayoutArrangerUsingMemberOrderFacet: createAssociationOrderSetFor " + spec.getFullIdentifier());
        }

        return DeweyOrderSet.createOrderSet(actionFacetedMethodList);
    }

}
