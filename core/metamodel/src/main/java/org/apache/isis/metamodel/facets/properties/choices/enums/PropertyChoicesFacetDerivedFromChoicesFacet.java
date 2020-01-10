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

package org.apache.isis.metamodel.facets.properties.choices.enums;

import org.apache.isis.core.commons.internal._Constants;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.FacetedMethod;
import org.apache.isis.metamodel.facets.objectvalue.choices.ChoicesFacet;
import org.apache.isis.metamodel.facets.properties.choices.PropertyChoicesFacetAbstract;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;

public class PropertyChoicesFacetDerivedFromChoicesFacet extends PropertyChoicesFacetAbstract {

    public PropertyChoicesFacetDerivedFromChoicesFacet(final FacetHolder holder) {
        super(holder);
    }

    @Override
    public Object[] getChoices(
            final ManagedObject adapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final FacetHolder facetHolder = getFacetHolder();
        final FacetedMethod facetedMethod = (FacetedMethod) facetHolder;
        final ObjectSpecification noSpec = getSpecification(facetedMethod.getType());
        final ChoicesFacet choicesFacet = noSpec.getFacet(ChoicesFacet.class);
        if (choicesFacet == null) {
            return _Constants.emptyObjects;
        }
        return choicesFacet.getChoices(adapter, interactionInitiatedBy);
    }

}
