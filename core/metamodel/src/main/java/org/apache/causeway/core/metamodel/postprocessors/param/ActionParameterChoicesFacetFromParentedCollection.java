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
package org.apache.causeway.core.metamodel.postprocessors.param;

import java.util.function.BiConsumer;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.collections.CollectionFacet;
import org.apache.causeway.core.metamodel.facets.param.choices.ActionParameterChoicesFacetAbstract;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;

import lombok.val;

public class ActionParameterChoicesFacetFromParentedCollection
extends ActionParameterChoicesFacetAbstract {

    private final OneToManyAssociation coll;

    public ActionParameterChoicesFacetFromParentedCollection(
            final FacetHolder holder,
            final OneToManyAssociation coll) {
        super(holder);
        this.coll = coll;
    }

    @Override
    public Can<ManagedObject> getChoices(
            final ObjectSpecification requiredSpec,
            final ActionInteractionHead head,
            final Can<ManagedObject> pendingArgs,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val collectionAsObject = coll.get(head.getOwner(), interactionInitiatedBy);
        return CollectionFacet.streamAdapters(collectionAsObject).collect(Can.toCan());
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("oneToManyAssociation", coll.getId());
    }
}
