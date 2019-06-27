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

package org.apache.isis.metamodel.facets.members.disabled;

import java.util.Map;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.wrapper.events.UsabilityEvent;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.WhereValueFacetAbstract;
import org.apache.isis.metamodel.interactions.ActionUsabilityContext;
import org.apache.isis.metamodel.interactions.UsabilityContext;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.specloader.specimpl.OneToManyAssociationContributee;
import org.apache.isis.metamodel.specloader.specimpl.OneToOneAssociationContributee;

public abstract class DisabledFacetAbstract extends WhereValueFacetAbstract implements DisabledFacet {

    public static Class<? extends Facet> type() {
        return DisabledFacet.class;
    }

    private final Semantics semantics;

    public enum Semantics {
        DISABLED,
        ENABLED;
    }

    public DisabledFacetAbstract(Where where, final FacetHolder holder) {
        this(where, holder, Semantics.DISABLED);
    }

    public DisabledFacetAbstract(
            final Where where,
            final FacetHolder holder,
            final Semantics semantics) {
        this(type(), where, holder, semantics);
    }

    protected DisabledFacetAbstract(
            final Class<? extends Facet> type,
            final Where where,
            final FacetHolder holder,
            final Semantics semantics) {
        super(type, holder, where);
        this.semantics = semantics;
    }

    @Override
    public String disables(final UsabilityContext<? extends UsabilityEvent> ic) {
        if(isInvertedSemantics()) {
            return null;
        }

        if(ic instanceof ActionUsabilityContext && (getFacetHolder() instanceof OneToOneAssociationContributee || getFacetHolder() instanceof OneToManyAssociationContributee)) {
            // otherwise ends up vetoing the invocation of the contributing action
            return null;
        }
        final ManagedObject target = ic.getTarget();
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

    @Override
    public boolean isInvertedSemantics() {
        return semantics == Semantics.ENABLED;
    }

    @Override
    public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("semantics", semantics);
        attributeMap.put("inverted", isInvertedSemantics());
    }

}
