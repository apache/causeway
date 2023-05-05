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

import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidatorAbstract;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailureUtils;

/**
 * Checks various preconditions for a sane meta-model.
 * <ul>
 *
 * <li>Guard against members that contribute vetoed or managed types.
 * Those are not allowed as member/return/param.</li>
 * </ul>
 */
public class SanityChecksValidator
extends MetaModelValidatorAbstract
implements
    MetaModelValidator.ActionValidator,
    MetaModelValidator.ParameterValidator,
    MetaModelValidator.PropertyValidator,
    MetaModelValidator.CollectionValidator {

    @Inject
    public SanityChecksValidator(final MetaModelContext mmc) {
        super(mmc, SKIP_MIXINS);
    }

    @Override
    public void validateParameter(final ObjectSpecification objectSpecification, final ObjectAction objectAction, final ObjectActionParameter parameter) {
        checkElementType(parameter, objectSpecification, parameter.getElementType());
    }

    @Override
    public void validateAction(final ObjectSpecification objectSpecification, final ObjectAction objectAction) {
        checkElementType(objectAction, objectSpecification, objectAction.getElementType());
    }

    @Override
    public void validateProperty(final ObjectSpecification objectSpecification, final OneToOneAssociation prop) {
        checkElementType(prop, objectSpecification, prop.getElementType());
    }

    @Override
    public void validateCollection(final ObjectSpecification objectSpecification, final OneToManyAssociation coll) {
        checkElementType(coll, objectSpecification, coll.getElementType());
    }

    @Override
    public void validateObjectEnter(final ObjectSpecification objSpec) {
        // guard against recursive call
        _Assert.assertFalse(hasEntered, ()->"framework bug: "
                + "validators are not expected to be called recursevely (nested)");
        this.hasEntered = true;
    }

    @Override
    public void validateObjectExit(final ObjectSpecification objSpec) {
        hasEntered = false;
    }

    // -- HELPER

    private boolean hasEntered = false; // validator recursive call guard

    private void checkElementType(
            final FacetHolder facetHolder,
            final ObjectSpecification declaringType,
            final ObjectSpecification elementType) {

        if(elementType == null
                || elementType.getBeanSort().isManagedBeanAny()
                || elementType.getBeanSort().isMixin()
                || elementType.getBeanSort().isVetoed()) {

            ValidationFailureUtils.raiseInvalidMemberElementType(facetHolder, declaringType, elementType);
        }
    }

}
