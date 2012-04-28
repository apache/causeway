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

import org.apache.log4j.Logger;

import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.layout.MemberLayoutArranger;
import org.apache.isis.core.metamodel.layout.OrderSet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class MemberLayoutArrangerUsingMemberOrderFacet implements MemberLayoutArranger {

    private static final Logger LOG = Logger.getLogger(MemberLayoutArrangerUsingMemberOrderFacet.class);

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

        return DeweyOrderSet.createOrderSet(associationMethods);
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
