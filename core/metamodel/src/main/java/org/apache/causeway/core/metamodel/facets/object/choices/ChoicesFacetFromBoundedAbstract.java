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
package org.apache.causeway.core.metamodel.facets.object.choices;

import java.util.Optional;

import org.apache.causeway.applib.query.Query;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.Consent.VetoReason;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.objectvalue.choices.ChoicesFacet;
import org.apache.causeway.core.metamodel.interactions.DisablingInteractionAdvisor;
import org.apache.causeway.core.metamodel.interactions.ValidatingInteractionAdvisor;
import org.apache.causeway.core.metamodel.interactions.use.UsabilityContext;
import org.apache.causeway.core.metamodel.interactions.val.ObjectValidityContext;
import org.apache.causeway.core.metamodel.interactions.val.ValidityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmVisibilityUtils;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager.BulkLoadRequest;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.SneakyThrows;

/**
 * A fixed number of choices because the number of instances of this class is bounded.
 *
 * <p>
 * Typically viewers will interpret this information by displaying all instances
 * of the class in a drop-down list box or similar widget.
 *
 * <p>
 * In the standard Apache Causeway Programming Model, corresponds to annotating the
 * member with {@link org.apache.causeway.applib.annotation.Bounding Bounding} annotation.
 */
public abstract class ChoicesFacetFromBoundedAbstract
extends FacetAbstract
implements
    ChoicesFacet,
    DisablingInteractionAdvisor,
    ValidatingInteractionAdvisor {

    private static final Class<? extends Facet> type() {
        return ChoicesFacet.class;
    }

    protected ChoicesFacetFromBoundedAbstract(
            final FacetHolder holder) {
        super(type(), holder);
    }

    @Override
    public String invalidates(final ValidityContext context) {
        if (!(context instanceof ObjectValidityContext)) {
            return null;
        }
        final ManagedObject target = context.target();
        if(target == null) return null;

        // ensure that the target is of the correct type (unexpected)
        if(!(getFacetHolder() instanceof ObjectSpecification)) return null;

        final ObjectSpecification objectSpec = getObjectSpecification();
        return objectSpec == target.getSpecification()? null: "Invalid type";
    }

    private ObjectSpecification getObjectSpecification() {
        return (ObjectSpecification) getFacetHolder();
    }

    @Override
    public Optional<VetoReason> disables(final UsabilityContext context) {
        final ManagedObject target = context.target();
        return disabledReason(target);
    }

    /**
     * Optional hook method for subclasses to override.
     */
    public Optional<VetoReason> disabledReason(final ManagedObject inObject) {
        return VetoReason.bounded().toOptional();
    }

    @SneakyThrows
    @Override
    public Can<ManagedObject> getChoices(
            final ManagedObject adapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        var resulType = getObjectSpecification().getCorrespondingClass();
        var query = Query.allInstances(resulType);

        var resultTypeSpec = specForType(resulType).orElse(null);
        var queryRequest = new BulkLoadRequest(resultTypeSpec, query);
        var allMatching = getObjectManager().queryObjects(queryRequest)
                .filter(MmVisibilityUtils.filterOn(interactionInitiatedBy));

        return allMatching;
    }

}
