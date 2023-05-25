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

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidatorAbstract;

/**
 * Checks various preconditions for a sane meta-model.
 * <ul>
 *
 * <li>Guard against members that contribute vetoed or managed types.
 * Those are not allowed as member/return/param.</li>
 * </ul>
 */
public class SanityChecksValidator2
extends MetaModelValidatorAbstract
implements
    MetaModelValidator.ActionValidator,
    MetaModelValidator.PropertyValidator,
    MetaModelValidator.CollectionValidator {

    @Inject
    public SanityChecksValidator2(final MetaModelContext mmc) {
        super(mmc, MIXINS);
    }

    @Override
    public void validateObjectEnter(final ObjectSpecification objSpec) {
        System.err.printf("mixin %s%n", objSpec);
    }

    @Override
    public void validateObjectExit(final ObjectSpecification objSpec) {
    }

    @Override
    public void validateAction(final ObjectSpecification objectSpecification, final ObjectAction objectAction) {

    }

    @Override
    public void validateProperty(final ObjectSpecification objectSpecification, final OneToOneAssociation prop) {

    }

    @Override
    public void validateCollection(final ObjectSpecification objectSpecification, final OneToManyAssociation coll) {

    }

    // -- HELPER

//    private void checkElementType(
//            final FacetHolder facetHolder,
//            final ObjectSpecification declaringType,
//            final ObjectSpecification elementType) {
//
//        if(elementType == null
//                || elementType.getBeanSort().isManagedBeanAny()
//                || elementType.getBeanSort().isMixin()
//                || elementType.getBeanSort().isVetoed()) {
//
//            ValidationFailureUtils.raiseInvalidMemberElementType(facetHolder, declaringType, elementType);
//        }
//    }

}
