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
package org.apache.causeway.core.metamodel.facets.actions.synthetic;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facets.actions.validate.ActionValidationFacet;
import org.apache.causeway.core.metamodel.interactions.ActionValidityContext;
import org.apache.causeway.core.metamodel.interactions.ValidityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;

import lombok.NonNull;
import lombok.val;

public class ActionValidationFacetForParentedCollectionNavigation
extends FacetAbstract
implements ActionValidationFacet {

    private static Class<? extends Facet> type() {
        return ActionValidationFacet.class;
    }

    private final @NonNull OneToManyAssociation collection;
    private final @NonNull Can<ObjectAssociation> filterProperties;

    public ActionValidationFacetForParentedCollectionNavigation(
            final @NonNull OneToManyAssociation collection,
            final @NonNull Can<ObjectAssociation> filterProperties,
            final @NonNull org.apache.causeway.core.metamodel.facetapi.FacetHolder holder) {
        super(type(), holder);
        this.collection = collection;
        this.filterProperties = filterProperties;
    }

    @Override
    public String invalidates(final ValidityContext context) {
        if (!(context instanceof ActionValidityContext)) {
            return null;
        }
        val actionValidityContext = (ActionValidityContext) context;
        return invalidReason(
                actionValidityContext.getObjectAction().getId(),
                actionValidityContext.getArgs(),
                actionValidityContext.getInitiatedBy());
    }

    @Override
    public String invalidReason(
            final ManagedObject target,
            final Can<ManagedObject> arguments) {
        return invalidReason(getFacetHolder().getFeatureIdentifier().memberLogicalName(), arguments, InteractionInitiatedBy.USER);
    }

    private String invalidReason(
            final @NonNull String actionId,
            final Can<ManagedObject> arguments,
            final @NonNull InteractionInitiatedBy interactionInitiatedBy) {
        val matchResult = ParentedCollectionNavigationMatchingUtil.match(
                collection,
                filterProperties,
                arguments,
                interactionInitiatedBy);
        return ParentedCollectionNavigationMatchingUtil.validationMessage(actionId, matchResult);
    }

}
