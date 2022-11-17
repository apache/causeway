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
package org.apache.causeway.core.metamodel.facets.param.choices;

import java.util.Optional;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.objectvalue.choices.ChoicesFacet;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

public class ActionParameterChoicesFacetFromChoicesFacet
extends ActionParameterChoicesFacetAbstract {

    public static Optional<ActionParameterChoicesFacet> create(
            final Optional<ChoicesFacet> choicesFacetIfAny,
            final FacetHolder facetHolder) {
        return choicesFacetIfAny
        .map(choicesFacet->new ActionParameterChoicesFacetFromChoicesFacet(choicesFacet, facetHolder));
    }

    private final ChoicesFacet choicesFacet;

    private ActionParameterChoicesFacetFromChoicesFacet(
            final ChoicesFacet choicesFacet,
            final FacetHolder holder) {
        super(holder, Precedence.INFERRED);
        this.choicesFacet = choicesFacet;
    }

    @Override
    public Can<ManagedObject> getChoices(
            final ObjectSpecification requiredSpec,
            final ActionInteractionHead head,
            final Can<ManagedObject> pendingArgs,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return choicesFacet.getChoices(head.getTarget(), interactionInitiatedBy);
    }

}
