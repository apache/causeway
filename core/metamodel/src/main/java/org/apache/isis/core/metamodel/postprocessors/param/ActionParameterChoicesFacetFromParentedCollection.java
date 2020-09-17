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

package org.apache.isis.core.metamodel.postprocessors.param;

import java.util.Map;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

import lombok.val;

public class ActionParameterChoicesFacetFromParentedCollection extends ActionParameterChoicesFacetAbstract {

    private final OneToManyAssociation otma;

    public ActionParameterChoicesFacetFromParentedCollection(
            final FacetHolder holder,
            final OneToManyAssociation otma) {
        super(holder);
        this.otma = otma;
    }

    @Override
    public Can<ManagedObject> getChoices(
            final ObjectSpecification requiredSpec,
            final ManagedObject target,
            final Can<ManagedObject> pendingArgs,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val parentAdapter = determineParentAdapter(target);
        val objectAdapter = otma.get(parentAdapter, interactionInitiatedBy);
        return CollectionFacet.streamAdapters(objectAdapter).collect(Can.toCan());
    }

    /**
     * in the case of a mixin action, the target passed to the facet is actually the mixin itself, 
     * not the mixee.
     */
    private ManagedObject determineParentAdapter(final ManagedObject target) {
        val mixinFacet = target.getSpecification().getFacet(MixinFacet.class);
        ManagedObject mixedInTarget = null;
        if(mixinFacet != null) {
            mixedInTarget = mixinFacet.mixedIn(target, MixinFacet.Policy.FAIL_FAST);
        }
        return mixedInTarget != null ? mixedInTarget : target;
    }

    @Override 
    public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("oneToManyAssociation", otma);
    }
}
