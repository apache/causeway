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
package org.apache.causeway.core.metamodel.specloader.validator;

import java.lang.annotation.Annotation;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ValidationFailureUtils {

    public <A extends Annotation> void raiseAmbiguousMixinAnnotations(
            final FacetedMethod holder,
            final Class<A> annotationType) {
        ValidationFailure.raise(holder,
                ProgrammingModelConstants.MessageTemplate.AMBIGUOUS_MIXIN_ANNOTATIONS
                    .builder()
                    .addVariable("annot", "@" + annotationType.getSimpleName())
                    .addVariable("mixinType", holder.getFeatureIdentifier().getFullIdentityString())
                    .buildMessage());
    }

    public void raiseMemberIdClash(
            final ObjectSpecification declaringType,
            final ObjectMember memberA,
            final ObjectMember memberB) {
        ValidationFailure.raise(memberB,
                ProgrammingModelConstants.MessageTemplate.MEMBER_ID_CLASH
                    .builder()
                    .addVariable("type", declaringType.fqcn())
                    .addVariable("memberId", ""+memberB.getId())
                    .addVariable("member1", memberA.getFeatureIdentifier().getFullIdentityString())
                    .addVariable("member2", memberB.getFeatureIdentifier().getFullIdentityString())
                    .buildMessage());
    }

    public void raiseMemberInvalidElementType(
            final FacetHolder facetHolder,
            final ObjectSpecification declaringType,
            final ObjectSpecification elementType) {
        ValidationFailure.raise(facetHolder,
                ProgrammingModelConstants.MessageTemplate.MEMBER_INVALID_ELEMENT_TYPE.builder()
                    .addVariable("type", declaringType.fqcn())
                    .addVariable("elementType", ""+elementType)
                    .buildMessage());
    }

    public <A extends Annotation> void raiseMemberInvalidAnnotation(
            final FacetedMethod facetedMethod,
            final Class<A> annotationType) {
        ValidationFailure.raise(facetedMethod, formatMemberInvalidAnnotation(facetedMethod.getFeatureIdentifier(), annotationType));
    }
    public <A extends Annotation> String formatMemberInvalidAnnotation(
            final Identifier identifier,
            final Class<A> annotationType) {
        return ProgrammingModelConstants.MessageTemplate.MEMBER_INVALID_ANNOTATION.builder()
            .addVariable("member", identifier.getFullIdentityString())
            .addVariable("annot", "@" + annotationType.getSimpleName())
            .buildMessage();
    }

}
