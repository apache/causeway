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

import java.util.List;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.collparam.semantics.CollectionSemanticsFacet;
import org.apache.isis.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionContributee;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionMixedIn;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

/**
 * Ensures that every action that has a collection parameter has a choices facet for that parameter.
 */
public class ActionChoicesForCollectionParameterFacetFactory extends FacetFactoryAbstract
implements MetaModelValidatorRefiner {

    public static final String ISIS_REFLECTOR_VALIDATOR_ACTION_COLLECTION_PARAMETER_CHOICES_KEY =
            "isis.reflector.validator.actionCollectionParameterChoices";
    public static final boolean ISIS_REFLECTOR_VALIDATOR_ACTION_COLLECTION_PARAMETER_CHOICES_DEFAULT = true;

    public ActionChoicesForCollectionParameterFacetFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        // no-op here... just adding a validator

    }

    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator, final IsisConfiguration configuration) {

        final boolean doCheck = configuration.getBoolean(
                ISIS_REFLECTOR_VALIDATOR_ACTION_COLLECTION_PARAMETER_CHOICES_KEY,
                ISIS_REFLECTOR_VALIDATOR_ACTION_COLLECTION_PARAMETER_CHOICES_DEFAULT);

        if(!doCheck) {
            return;
        }

        final MetaModelValidator validator = new MetaModelValidatorVisiting(
                new MetaModelValidatorVisiting.Visitor() {
                    @Override
                    public boolean visit(
                            final ObjectSpecification objectSpec,
                            final ValidationFailures validationFailures) {
                        validate(objectSpec, validationFailures);
                        return true;
                    }

                    private void validate(
                            final ObjectSpecification objectSpec,
                            final ValidationFailures validationFailures) {
                        objectSpec.streamObjectActions(Contributed.INCLUDED)
                        .forEach(objectAction->{
                            if(objectAction instanceof ObjectActionMixedIn || objectAction instanceof ObjectActionContributee) {
                                // we'll report only the mixin or contributor
                                return;
                            }

                            int paramNum = 0;
                            for (ObjectActionParameter parameter : objectAction.getParameters()) {
                                if(parameter.getFeatureType() == FeatureType.ACTION_PARAMETER_COLLECTION) {
                                    validate(objectSpec, objectAction, parameter, paramNum, validationFailures);
                                }
                                paramNum++;
                            }
                        });
                    }

                    private void validate(
                            final ObjectSpecification objectSpec,
                            final ObjectAction objectAction,
                            final ObjectActionParameter parameter,
                            final int paramNum,
                            final ValidationFailures validationFailures) {


                        final CollectionSemanticsFacet collectionSemantics =
                                parameter.getFacet(CollectionSemanticsFacet.class);
                        if (collectionSemantics != null) {
                            // Violation if there are action parameter types that are assignable
                            // from java.util.Collection but are not of
                            // exact type List, Set, SortedSet or Collection.
                            if(!collectionSemantics.value().isSupportedInterfaceForActionParameters()) {
                                validationFailures.add(
                                        "Collection action parameter found that is not exactly one "
                                                + "of the following supported types: "
                                                + "List, Set, SortedSet, Collection or Array.  "
                                                + "Class: %s action: %s parameter %d",
                                                objectSpec.getFullIdentifier(), objectAction.getName(), paramNum);
                                return;
                            }
                        }

                        final ActionParameterChoicesFacet choicesFacet =
                                parameter.getFacet(ActionParameterChoicesFacet.class);
                        final ActionParameterAutoCompleteFacet autoCompleteFacet =
                                parameter.getFacet(ActionParameterAutoCompleteFacet.class);
                        if (choicesFacet != null || autoCompleteFacet != null) {
                            return;
                        }

                        final ObjectSpecification parameterType = parameter.getSpecification();
                        if(parameterType.containsDoOpFacet(AutoCompleteFacet.class)) {
                            return;
                        }

                        validationFailures.add(
                                "Collection action parameter found without supporting "
                                        + "choices or autoComplete facet.  "
                                        + "Class: %s action: %s parameter %d",
                                        objectSpec.getFullIdentifier(), objectAction.getName(), paramNum);
                    }
                });
        metaModelValidator.add(validator);
    }

}
