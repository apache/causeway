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
package org.apache.causeway.core.metamodel.facets.actions.validate;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.interactions.val.ParamValidityContext;
import org.apache.causeway.core.metamodel.interactions.val.ValidityContext;

public abstract class ActionParameterValidationFacetAbstract
extends FacetAbstract
implements ActionParameterValidationFacet {

    private static final Class<? extends Facet> type() {
        return ActionParameterValidationFacet.class;
    }

    public ActionParameterValidationFacetAbstract(final FacetHolder holder) {
        super(type(), holder);
    }

    @Override
    public String invalidates(final ValidityContext context) {
        if (!(context instanceof ParamValidityContext)) return null;

        final ParamValidityContext actionArgValidityContext = (ParamValidityContext) context;
        return invalidReason(actionArgValidityContext.target(), actionArgValidityContext.proposed());
    }
}
