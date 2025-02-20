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
package org.apache.causeway.core.metamodel.facets.param.disable;

import java.util.Optional;

import org.apache.causeway.core.metamodel.consent.Consent.VetoReason;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.interactions.ActionArgUsabilityContext;
import org.apache.causeway.core.metamodel.interactions.UsabilityContext;

public abstract class ActionParameterDisabledFacetAbstract
extends FacetAbstract
implements ActionParameterDisabledFacet {

    private static final Class<? extends Facet> type() {
        return ActionParameterDisabledFacet.class;
    }

    public ActionParameterDisabledFacetAbstract(final FacetHolder holder) {
        super(type(), holder);
    }

    @Override
    public Optional<VetoReason> disables(final UsabilityContext context) {
        if (!(context instanceof ActionArgUsabilityContext)) {
            return Optional.empty();
        }
        final ActionArgUsabilityContext actionArgUsabilityContext = (ActionArgUsabilityContext) context;
        return disabledReason(actionArgUsabilityContext.target(), actionArgUsabilityContext.args());
    }
}
