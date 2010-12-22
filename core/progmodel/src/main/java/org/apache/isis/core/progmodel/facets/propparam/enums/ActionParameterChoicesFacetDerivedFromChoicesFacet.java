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

package org.apache.isis.core.progmodel.facets.propparam.enums;

import org.apache.isis.core.metamodel.adapter.AdapterMap;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.TypedHolder;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLookup;
import org.apache.isis.core.progmodel.facets.actions.choices.ActionParameterChoicesFacetAbstract;

public class ActionParameterChoicesFacetDerivedFromChoicesFacet extends ActionParameterChoicesFacetAbstract {

    public ActionParameterChoicesFacetDerivedFromChoicesFacet(FacetHolder holder,
        final SpecificationLookup specificationLookup, final AdapterMap adapterManager) {
        super(holder, specificationLookup, adapterManager);
    }

    @Override
    public Object[] getChoices(ObjectAdapter adapter) {
        FacetHolder facetHolder = getFacetHolder();
        TypedHolder paramPeer = (TypedHolder) facetHolder;
        ObjectSpecification noSpec = getSpecification(paramPeer.getType());
        ChoicesFacet choicesFacet = noSpec.getFacet(ChoicesFacet.class);
        if (choicesFacet == null)
            return new Object[0];
        return choicesFacet.getChoices(adapter);
    }

}
