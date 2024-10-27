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
package org.apache.causeway.core.metamodel.postprocessors.all;

import javax.inject.Inject;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.actions.contributing.ContributingFacet;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet.Contributing;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidatorAbstract;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

/**
 * Checks various preconditions for a sane meta-model.
 * <ul>
 * <li>Guard against types that are identified as {@link BeanSort#MIXIN}, but don't contribute any member.</li>
 * <li>Make sure mixins contribute either an action or a property or a collection,
 *  and if declared via {@link Action}, {@link Property} or {@link Collection} annotation,
 *  that the {@link Contributing} is correct.</li>
 * <li>Make sure if a mixin main method name is specified (eg. 'act', 'prop', 'coll'),
 *  that introspection was able to pick it up.</li>
 * </ul>
 */
public class MixinSanityChecksValidator
extends MetaModelValidatorAbstract
implements
    MetaModelValidator.ActionValidator {

    private Contributing contributing;

    @Inject
    public MixinSanityChecksValidator(final MetaModelContext mmc) {
        super(mmc, MIXINS);
    }

    @Override
    public void validateObjectEnter(final ObjectSpecification objSpec) {
        final Contributing contributing = objSpec.contributing().orElse(null);
        if(contributing==null
                || contributing.isUnspecified()) {
            ValidationFailure.raiseFormatted(objSpec,
                    ProgrammingModelConstants.MessageTemplate.INVALID_MIXIN_TYPE
                        .builder()
                        .addVariable("type", objSpec.getCorrespondingClass().getName())
                        .buildMessage());
            return;
        }
        this.contributing = contributing;
    }

    @Override
    public void validateObjectExit(final ObjectSpecification objSpec) {
        this.contributing = null;
    }

    /*
     * (introspected) mixins have no properties nor collections; instead the single member is always
     * an action that either contributes as action, property or collection
     */
    @Override
    public void validateAction(final ObjectSpecification objSpec, final ObjectAction act) {
        if(contributing==null) return; // skip if already failed earlier
        if(act.isMixedIn()) return; // don't process mixed in actions (that were mixed in to the mixin under validation)
        checkMixinMainMethod(objSpec, act.getFeatureIdentifier());
        checkMixinSort(objSpec, (FacetedMethod) act.getFacetHolder());
    }

    // -- HELPER

    private void checkMixinSort(final ObjectSpecification objSpec, final FacetedMethod facetedMethod) {
        var expectedContributing = facetedMethod.lookupFacet(ContributingFacet.class)
            .map(ContributingFacet::contributed)
            .orElse(Contributing.AS_ACTION); // if not specified, defaults to action
        var actualContributing = this.contributing;

        if(actualContributing!=expectedContributing) {
            ValidationFailure.raiseFormatted(objSpec,
                    ProgrammingModelConstants.MessageTemplate.INVALID_MIXIN_SORT
                        .builder()
                        .addVariable("type", objSpec.getCorrespondingClass().getName())
                        .addVariable("expectedContributing", expectedContributing.name())
                        .addVariable("actualContributing", actualContributing.name())
                        .buildMessage());
        }
    }

    private void checkMixinMainMethod(final ObjectSpecification objSpec, final Identifier memberIdentifier) {
        var mixinFacet = ((ObjectSpecificationAbstract)objSpec).mixinFacet().orElseThrow();

        var expectedMethodName = mixinFacet.getMainMethodName();
        var actualMethodName = memberIdentifier.getMemberLogicalName();

        if(!expectedMethodName.equals(actualMethodName)) {
            ValidationFailure.raiseFormatted(objSpec,
                    ProgrammingModelConstants.MessageTemplate.INVALID_MIXIN_MAIN
                        .builder()
                        .addVariable("type", objSpec.getCorrespondingClass().getName())
                        .addVariable("expectedMethodName", expectedMethodName)
                        .addVariable("actualMethodName", actualMethodName)
                        .buildMessage());
        }
    }

}
