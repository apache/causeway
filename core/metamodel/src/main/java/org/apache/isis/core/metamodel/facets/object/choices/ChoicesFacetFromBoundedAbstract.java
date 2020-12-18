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

package org.apache.isis.core.metamodel.facets.object.choices;

import org.apache.isis.applib.query.Query;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.objectvalue.choices.ChoicesFacet;
import org.apache.isis.core.metamodel.interactions.DisablingInteractionAdvisor;
import org.apache.isis.core.metamodel.interactions.ObjectValidityContext;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.ValidatingInteractionAdvisor;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.objectmanager.query.ObjectBulkLoader;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.SneakyThrows;
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
    public String invalidates(final ValidityContext context) {
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
    public String disables(final UsabilityContext context) {
        final ManagedObject target = context.getTarget();
        return disabledReason(target);
    }

    /**
     * Optional hook method for subclasses to override.
     */
    public String disabledReason(final ManagedObject inObject) {
        return "Bounded";
    }

    @SneakyThrows
    @Override
    public Can<ManagedObject> getChoices(
            final ManagedObject adapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        //TODO[2033] if assert is always true just use type = getObjectSpecification().getCorrespondingClass()
        val resulType = _Context.loadClassAndInitialize(getObjectSpecification().getFullIdentifier());
        _Assert.assertEquals(
                getObjectSpecification().getCorrespondingClass().getName(), 
                getObjectSpecification().getFullIdentifier());
        
        val query = Query.allInstances(resulType);
        
        val resultTypeSpec = getObjectManager().loadSpecification(resulType);
        val queryRequest = ObjectBulkLoader.Request.of(resultTypeSpec, query);
        val allMatching = getObjectManager().queryObjects(queryRequest)
                .filter(ManagedObjects.VisibilityUtil.filterOn(interactionInitiatedBy));
        
        return allMatching;
    }

}
