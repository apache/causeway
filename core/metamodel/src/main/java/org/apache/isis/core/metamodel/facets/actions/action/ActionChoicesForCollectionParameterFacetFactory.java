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

package org.apache.isis.core.metamodel.facets.actions.action;

import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.actions.action.associateWith.ChoicesFromFacet;
import org.apache.isis.core.metamodel.facets.collparam.semantics.CollectionSemanticsFacet;
import org.apache.isis.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionMixedIn;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;

import lombok.val;

/**
 * Ensures that every action that has a collection parameter has a choices facet for that parameter.
 */
public class ActionChoicesForCollectionParameterFacetFactory extends FacetFactoryAbstract
implements MetaModelRefiner {

    public ActionChoicesForCollectionParameterFacetFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        // no-op here... just adding a validator

    }

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {

        val shouldCheck = getConfiguration().getCore().getMetaModel().getValidator().isActionCollectionParameterChoices();
        if(!shouldCheck) {
            return;
        }

        programmingModel.addVisitingValidatorSkipManagedBeans(objectSpec->{
            // as an optimization only checking declared members (skipping inherited ones)
            objectSpec.streamDeclaredActions(MixedIn.INCLUDED)
            .forEach(objectAction->{
                if(objectAction instanceof ObjectActionMixedIn) {
                    // we'll report only the mixin
                    return;
                }

                int paramNum = 0;
                for (ObjectActionParameter parameter : objectAction.getParameters()) {
                    if(parameter.getFeatureType() == FeatureType.ACTION_PARAMETER_COLLECTION) {
                        validateActionParameter_whenCollection(
                                objectSpec, objectAction, parameter, paramNum);
                    }
                    paramNum++;
                }
            });
        });

    }

    private static void validateActionParameter_whenCollection(
            final ObjectSpecification objectSpec,
            final ObjectAction objectAction,
            final ObjectActionParameter parameter,
            final int paramNum) {


        val collectionSemanticsFacet = parameter.getFacet(CollectionSemanticsFacet.class);
        if (collectionSemanticsFacet != null) {
            // Violation if there are action parameter types that are assignable
            // from java.util.Collection but are not of
            // exact type List, Set, SortedSet or Collection.
            if(!collectionSemanticsFacet.value().isSupportedInterfaceForActionParameters()) {

                val messageFormat = "Collection action parameter found that is not exactly one "
                        + "of the following supported types: "
                        + "List, Set, SortedSet, Collection or Array.  "
                        + "Class: %s action: %s parameter %d";

                ValidationFailure.raise(
                        objectSpec,
                        String.format(
                                messageFormat,
                                objectSpec.getFullIdentifier(),
                                objectAction.getName(),
                                paramNum));

                return;
            }
        }

        val actionParameterChoicesFacet = parameter.getFacet(ActionParameterChoicesFacet.class);
        val actionParameterAutoCompleteFacet = parameter.getFacet(ActionParameterAutoCompleteFacet.class);
        if (actionParameterChoicesFacet != null || actionParameterAutoCompleteFacet != null) {
            return;
        }

        val parameterSpec = parameter.getSpecification();
        if(parameterSpec.containsNonFallbackFacet(AutoCompleteFacet.class)) {
            return;
        }

        //TODO[2253] remove this hotfix once ISIS-2253 is fixed
        if(paramNum==0 && objectAction.containsNonFallbackFacet(ChoicesFromFacet.class)) {
            return;
        }

        val messageFormat = "Collection action parameter found without supporting "
                + "choices or autoComplete facet.  "
                + "Class: %s action: %s parameter %d";

        ValidationFailure.raise(
                objectSpec,
                String.format(
                        messageFormat,
                        objectSpec.getFullIdentifier(),
                        objectAction.getName(),
                        paramNum));
    }

}
