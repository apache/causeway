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

package org.apache.isis.core.metamodel.facets.members.disabled;

import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.events.UsabilityEvent;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.WhenAndWhereValueFacetAbstract;
import org.apache.isis.core.metamodel.interactions.ActionUsabilityContext;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.specloader.specimpl.OneToManyAssociationContributee;
import org.apache.isis.core.metamodel.specloader.specimpl.OneToOneAssociationContributee;

public abstract class DisabledFacetAbstract extends WhenAndWhereValueFacetAbstract implements DisabledFacet {

    public static Class<? extends Facet> type() {
        return DisabledFacet.class;
    }

    public DisabledFacetAbstract(final When when, Where where, final FacetHolder holder) {
        this(type(), when, where, holder);
    }

    private DisabledFacetAbstract(final Class<? extends Facet> type, final When when, Where where, final FacetHolder holder) {
        super(type, holder, when, where);
    }

    @Override
    public String disables(final UsabilityContext<? extends UsabilityEvent> ic) {
        if(ic instanceof ActionUsabilityContext && (getFacetHolder() instanceof OneToOneAssociationContributee || getFacetHolder() instanceof OneToManyAssociationContributee)) {
            // otherwise ends up vetoing the invocation of the contributing action
            return null;
        }
        final ObjectAdapter target = ic.getTarget();
        final String disabledReason = disabledReason(target);
        if (disabledReason != null) {
            return disabledReason;
        }
        if (getUnderlyingFacet() != null) {
            final DisabledFacet underlyingFacet = (DisabledFacet) getUnderlyingFacet();
            return underlyingFacet.disabledReason(target);
        }
        return null;
    }

}
