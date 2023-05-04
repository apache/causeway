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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidatorAbstract;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

/**
 * Checks various preconditions for a sane meta-model.
 * <ul>

 * <li>Guard against members and mixed-in members that share the same member-id.
 * <ul>
 *      <li>member-ids for actions within the same type must be unique (including mixed-in ones)</li>
 *      <li>member-ids for properties within the same type must be unique (including mixed-in ones)</li>
 *      <li>member-ids for collections within the same type must be unique (including mixed-in ones)</li>
 * </ul>
 * </li>
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
        checkMemberId(objectAction, objectSpecification);
        checkElementType(objectAction, objectSpecification, objectAction.getElementType());
    }

    @Override
    public void validateProperty(final ObjectSpecification objectSpecification, final OneToOneAssociation prop) {
        checkMemberId(prop, objectSpecification);
        checkElementType(prop, objectSpecification, prop.getElementType());
    }

    @Override
    public void validateCollection(final ObjectSpecification objectSpecification, final OneToManyAssociation coll) {
        checkMemberId(coll, objectSpecification);
        checkElementType(coll, objectSpecification, coll.getElementType());
    }

    @Override
    public void validateObjectEnter(final ObjectSpecification objSpec) {
        // guard against recursive call
        _Assert.assertNull(this.memberIdCollector, ()->"framework bug: "
                + "validators are not expected to be called recursevely (nested)");
        this.memberIdCollector = new MemberIdCollector();
    }

    @Override
    public void validateObjectExit(final ObjectSpecification objSpec) {
        memberIdCollector = null; // garbage collect
    }

    // -- HELPER

    private static class MemberIdCollector {
        private Map<String, ObjectMember> actionIds = new HashMap<>();
        private Map<String, ObjectMember> propertyIds = new HashMap<>();
        private Map<String, ObjectMember> collectionIds = new HashMap<>();
        /** Optionally returns a member with the same member-id, based on whether previously collected. */
        public Optional<ObjectMember> collect(final ObjectMember objectMember) {
            if(objectMember.isAction()) {
                return Optional.ofNullable(actionIds.put(objectMember.getId(), objectMember));
            }
            if(objectMember.isProperty()) {
                return Optional.ofNullable(propertyIds.put(objectMember.getId(), objectMember));
            }
            if(objectMember.isCollection()) {
                return Optional.ofNullable(collectionIds.put(objectMember.getId(), objectMember));
            }
            throw _Exceptions.unmatchedCase(String.format("framework bug: unmatched feature %s", objectMember));
        }
    }

    private MemberIdCollector memberIdCollector;

    private void checkMemberId(
            final ObjectMember objectMember,
            final ObjectSpecification declaringType) {

        if(declaringType.isAbstract()) return;

        //TODO[CAUSEWAY-3051] should be 18 but reports only 11
        //perhaps because we internally assume no clashing during spec-loading, which is wrong
        if(objectMember.getDeclaringType().toString().contains("InvalidMemberIdClash")) {
            System.err.printf("member-id: %s (%s)%n", objectMember.getId(), objectMember.getDeclaringType());
        }

        memberIdCollector.collect(objectMember)
        .ifPresent(previous->{
            ValidationFailure.raiseFormatted(objectMember,
                    ProgrammingModelConstants.Violation.MEMBER_ID_CLASH
                        .builder()
                        .addVariable("type", declaringType.fqcn())
                        .addVariable("memberId", ""+objectMember.getId())
                        .addVariable("member1", previous.getFeatureIdentifier().getFullIdentityString())
                        .addVariable("member2", objectMember.getFeatureIdentifier().getFullIdentityString())
                        .buildMessage());
        });
    }

    private void checkElementType(
            final FacetHolder facetHolder,
            final ObjectSpecification declaringType,
            final ObjectSpecification elementType) {

        if(elementType == null
                || elementType.getBeanSort().isManagedBeanAny()
                || elementType.getBeanSort().isMixin()
                || elementType.getBeanSort().isVetoed()) {

            ValidationFailure.raiseFormatted(facetHolder,
                    ProgrammingModelConstants.Violation.INVALID_MEMBER_ELEMENT_TYPE
                        .builder()
                        .addVariable("type", declaringType.fqcn())
                        .addVariable("elementType", ""+elementType)
                        .buildMessage());
        }
    }

}
