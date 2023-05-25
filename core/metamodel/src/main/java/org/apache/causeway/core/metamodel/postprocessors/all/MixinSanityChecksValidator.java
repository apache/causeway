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
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet.MixinSort;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidatorAbstract;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

import lombok.val;

/**
 * Checks various preconditions for a sane meta-model.
 * <ul>
 * <li>Guard against types that are identified as {@link BeanSort#MIXIN}, but don't contribute any member.</li>
 * <li>Make sure mixins contribute either an action or a property or a collection.</li>
 * <li>Make sure if a mixin main method name is specified (eg 'act', 'prop', 'coll'),
 *  that introspection was able to pick it up.</li>
 * </ul>
 */
public class MixinSanityChecksValidator
extends MetaModelValidatorAbstract
implements
    MetaModelValidator.ActionValidator,
    MetaModelValidator.PropertyValidator,
    MetaModelValidator.CollectionValidator {

    private MixinSort mixinSort;

    @Inject
    public MixinSanityChecksValidator(final MetaModelContext mmc) {
        super(mmc, MIXINS);
    }

    @Override
    public void validateObjectEnter(final ObjectSpecification objSpec) {
        final MixinSort mixinSort = objSpec.getMixinSort().orElse(null);
        if(mixinSort==null
                || mixinSort.isUnspecified()) {
            ValidationFailure.raiseFormatted(objSpec,
                    ProgrammingModelConstants.Violation.INVALID_MIXIN_TYPE
                        .builder()
                        .addVariable("type", objSpec.getCorrespondingClass().getName())
                        .buildMessage());
            return;
        }
        this.mixinSort = mixinSort;
    }

    @Override
    public void validateObjectExit(final ObjectSpecification objSpec) {
        this.mixinSort = null;
    }

    @Override
    public void validateAction(final ObjectSpecification objSpec, final ObjectAction act) {
        if(mixinSort==null) return; // skip if already failed earlier
        checkMixinMainMethod(objSpec, act.getFeatureIdentifier());
    }

    @Override
    public void validateProperty(final ObjectSpecification objSpec, final OneToOneAssociation prop) {
        if(mixinSort==null) return; // skip if already failed earlier
        checkMixinMainMethod(objSpec, prop.getFeatureIdentifier());
    }

    @Override
    public void validateCollection(final ObjectSpecification objSpec, final OneToManyAssociation coll) {
        if(mixinSort==null) return; // skip if already failed earlier
        checkMixinMainMethod(objSpec, coll.getFeatureIdentifier());
    }

    // -- HELPER

    private void checkMixinMainMethod(final ObjectSpecification objSpec, final Identifier memberIdentifier) {
        val mixinFacet = ((ObjectSpecificationAbstract)objSpec).mixinFacet().orElseThrow();

        val expectedMethodName = mixinFacet.getMainMethodName();
        val actualMethodName = memberIdentifier.getMemberLogicalName();

        if(!expectedMethodName.equals(actualMethodName)) {
            ValidationFailure.raiseFormatted(objSpec,
                    ProgrammingModelConstants.Violation.INVALID_MIXIN_MAIN
                        .builder()
                        .addVariable("type", objSpec.getCorrespondingClass().getName())
                        .addVariable("expectedMethodName", expectedMethodName)
                        .addVariable("actualMethodName", actualMethodName)
                        .buildMessage());
        }
    }

}
