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
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

import lombok.val;

public class ActionParameterChoicesFacetFromParentedCollection 
extends ActionParameterChoicesFacetAbstract {

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

        guardAgainstMixin(target);
        val collectionAdapter = otma.get(target, interactionInitiatedBy);
        return CollectionFacet.streamAdapters(collectionAdapter).collect(Can.toCan());
    }

    /**
     * in the case of a mixin action, the target passed to the facet is actually the mixin itself, 
     * not the mixee
     */
    private void guardAgainstMixin(final ManagedObject target) {
        val mixinFacet = target.getSpecification().getFacet(MixinFacet.class);
        if(mixinFacet != null) {
            _Exceptions.unrecoverable("internal error: choices facet invoked on mixin target, "
                    + "but should be a mixee target instead");
        }
    }

    @Override 
    public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("oneToManyAssociation", otma);
    }
}
