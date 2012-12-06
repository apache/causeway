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
package org.apache.isis.core.metamodel.layout;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.log4j.Logger;

import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public abstract class MemberLayoutArrangerComposite implements MemberLayoutArranger {

    private static final Logger LOG = Logger.getLogger(MemberLayoutArrangerComposite.class);

    private final List<MemberLayoutArranger> arrangers = Lists.newArrayList();

    // ////////////////////////////////////////////////////////////////////////////
    // constructor
    // ////////////////////////////////////////////////////////////////////////////

    public MemberLayoutArrangerComposite(final MemberLayoutArranger... arrangers) {
        this.arrangers.addAll(Arrays.asList(arrangers));
    }

    // ////////////////////////////////////////////////////////////////////////////
    // associations
    // ////////////////////////////////////////////////////////////////////////////

    @Override
    public OrderSet createAssociationOrderSetFor(final ObjectSpecification spec, final List<FacetedMethod> associationMethods) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("MemberLayoutArrangerComposite: createAssociationOrderSetFor " + spec.getFullIdentifier());
        }

        for (final MemberLayoutArranger arranger : arrangers) {
            final OrderSet orderSet = arranger.createAssociationOrderSetFor(spec, associationMethods);
            if (orderSet != null) {
                return orderSet;
            }
        }
        return null;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // actions
    // ////////////////////////////////////////////////////////////////////////////

    @Override
    public OrderSet createActionOrderSetFor(final ObjectSpecification spec, final List<FacetedMethod> actionMethods) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("MemberLayoutArrangerDefault: createAssociationOrderSetFor " + spec.getFullIdentifier());
        }

        for (final MemberLayoutArranger arranger : arrangers) {
            final OrderSet orderSet = arranger.createActionOrderSetFor(spec, actionMethods);
            if (orderSet != null) {
                return orderSet;
            }
        }
        return null;

    }

}
