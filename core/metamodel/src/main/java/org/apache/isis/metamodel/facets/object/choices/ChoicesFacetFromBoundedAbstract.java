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

package org.apache.isis.metamodel.facets.object.choices;

import java.util.function.Predicate;

import org.apache.isis.applib.services.wrapper.events.UsabilityEvent;
import org.apache.isis.applib.services.wrapper.events.ValidityEvent;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetAbstract;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.objectvalue.choices.ChoicesFacet;
import org.apache.isis.metamodel.interactions.DisablingInteractionAdvisor;
import org.apache.isis.metamodel.interactions.ObjectValidityContext;
import org.apache.isis.metamodel.interactions.UsabilityContext;
import org.apache.isis.metamodel.interactions.ValidatingInteractionAdvisor;
import org.apache.isis.metamodel.interactions.ValidityContext;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;

import lombok.val;

/**
 * A fixed number of choices because the number of instances of this class is bounded.
 *
 * <p>
 * Typically viewers will interpret this information by displaying all instances
 * of the class in a drop-down list box or similar widget.
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to annotating the
 * member with {@link org.apache.isis.applib.annotation.Bounded Bounded} annotation
 * or implementing the {@link Bounded} marker interface.
 */
public abstract class ChoicesFacetFromBoundedAbstract
extends FacetAbstract
implements ChoicesFacet, DisablingInteractionAdvisor, ValidatingInteractionAdvisor {

    public static Class<? extends Facet> type() {
        return ChoicesFacet.class;
    }

    public ChoicesFacetFromBoundedAbstract(
            final FacetHolder holder) {
        super(type(), holder, Derivation.NOT_DERIVED);
    }

    @Override
    public String invalidates(final ValidityContext<? extends ValidityEvent> context) {
        if (!(context instanceof ObjectValidityContext)) {
            return null;
        }
        final ManagedObject target = context.getTarget();
        if(target == null) {
            return null;
        }

        // ensure that the target is of the correct type
        if(!(getFacetHolder() instanceof ObjectSpecification)) {
            // should never be the case
            return null;
        }

        final ObjectSpecification objectSpec = getObjectSpecification();
        return objectSpec == target.getSpecification()? null: "Invalid type";
    }

    private ObjectSpecification getObjectSpecification() {
        return (ObjectSpecification) getFacetHolder();
    }

    @Override
    public String disables(final UsabilityContext<? extends UsabilityEvent> context) {
        final ManagedObject target = context.getTarget();
        return disabledReason(target);
    }

    /**
     * Optional hook method for subclasses to override.
     */
    public String disabledReason(final ManagedObject inObject) {
        return "Bounded";
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Object[] getChoices(
            ManagedObject adapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val context = getMetaModelContext();
        val repository = context.getRepositoryService();

        final Predicate<ManagedObject> visibilityFilter = 
                objectAdapter -> ManagedObject.Visibility.isVisible(objectAdapter, interactionInitiatedBy); 

        val query = new QueryFindAllChoices(getObjectSpecification().getFullIdentifier(), visibilityFilter);
        return repository.allMatches(query).toArray();
    }

}
