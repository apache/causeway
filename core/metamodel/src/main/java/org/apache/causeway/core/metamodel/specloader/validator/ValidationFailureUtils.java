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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.functions._Predicates;
import org.apache.causeway.commons.internal.primitives._Ints;
import org.apache.causeway.commons.internal.primitives._Ints.Range;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.facetapi.Facet.Precedence;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ValidationFailureUtils {

    public <A extends Annotation> void raiseAmbiguousMixinAnnotations(
            final FacetedMethod holder,
            final Class<A> annotationType) {

        ValidationFailure.raiseFormatted(holder,
                ProgrammingModelConstants.Violation.AMBIGUOUS_MIXIN_ANNOTATIONS
                    .builder()
                    .addVariable("annot", "@" + annotationType.getSimpleName())
                    .addVariable("mixinType", holder.getFeatureIdentifier().getFullIdentityString())
                    .buildMessage());
    }


    public void raiseMemberIdClash(
            final ObjectSpecification declaringType,
            final ObjectMember memberA,
            final ObjectMember memberB) {

        ValidationFailure.raiseFormatted(memberB,
                ProgrammingModelConstants.Violation.MEMBER_ID_CLASH
                    .builder()
                    .addVariable("type", declaringType.fqcn())
                    .addVariable("memberId", ""+memberB.getId())
                    .addVariable("member1", memberA.getFeatureIdentifier().getFullIdentityString())
                    .addVariable("member2", memberB.getFeatureIdentifier().getFullIdentityString())
                    .buildMessage());
    }

    public void raiseInvalidMemberElementType(
            final FacetHolder facetHolder,
            final ObjectSpecification declaringType,
            final ObjectSpecification elementType) {
        ValidationFailure.raiseFormatted(facetHolder,
                ProgrammingModelConstants.Violation.INVALID_MEMBER_ELEMENT_TYPE
                    .builder()
                    .addVariable("type", declaringType.fqcn())
                    .addVariable("elementType", ""+elementType)
                    .buildMessage());
    }


    private static final Range PRECEDENCE_ORDINALS_CONSIDERED_FOR_CONFLICTING_OPTIONALITY =
            _Ints.rangeClosed(Precedence.LOW.ordinal(), Precedence.HIGH.ordinal());

    /**
     * As called on the fly during {@link MandatoryFacet} creation,
     * not only checks the final top rank but also intermediate top ranks.
     * However, only considering {LOW, DEFAULT and HIGH}. Others are ignored.
     *
     * @apiNote as an alternative we could do a check only on the top rank once the MM is fully populated
     */
    public void raiseIfConflictingOptionality(
            final @Nullable MandatoryFacet mandatoryFacet, final Supplier<String> messageSupplier) {

        if(mandatoryFacet == null
                || !PRECEDENCE_ORDINALS_CONSIDERED_FOR_CONFLICTING_OPTIONALITY
                    .contains(mandatoryFacet.getPrecedence().ordinal())) {
            return; // ignore
        }

        final Can<MandatoryFacet> conflictingFacets = conflictingMandatoryFacets(mandatoryFacet);


        if(conflictingFacets.isNotEmpty()) {
            val holder = mandatoryFacet.getFacetHolder();

            ValidationFailure.raiseFormatted(holder,
                    ProgrammingModelConstants.Violation.CONFLICTING_OPTIONALITY.builder()
                        .addVariable("member", holder.getFeatureIdentifier().getFullIdentityString())
                        .addVariable("conflictingFacets", conflictingFacets.stream()
                                .map(MandatoryFacet::summarize)
                                .collect(Collectors.joining(", \n")))
                        .buildMessage());
        }
    }

    // -- HELPER

    private Can<MandatoryFacet> conflictingMandatoryFacets(final MandatoryFacet mandatoryFacet) {

        //TODO maybe move this kind of logic to FacetRanking

        val facetRanking = mandatoryFacet.getSharedFacetRankingElseFail();

        // assumes that given mandatoryFacet is one of the top ranking

        val isTopRanking = mandatoryFacet.getPrecedence()
                .equals(facetRanking.getTopPrecedence().orElse(null));
        if(!isTopRanking) {
            // ignore validation of lower than top-rank
            return Can.empty();
        }

        val topRankingFacets = facetRanking.getTopRank(mandatoryFacet.facetType());
        val firstOfTopRanking = (MandatoryFacet)topRankingFacets.getFirstElseFail();

        // the top ranking mandatory facets should semantically agree
        final Can<MandatoryFacet> conflictingWithFirst = topRankingFacets.isCardinalityMultiple()
                ? topRankingFacets
                        .stream()
                        .skip(1)
                        .map(MandatoryFacet.class::cast) // upcast
                        .filter(_Predicates.not(firstOfTopRanking::semanticEquals))
                        .collect(Can.toCan())
                : Can.empty(); // not conflicting

        // if there are any conflicts, prepend first facet
        return conflictingWithFirst.isEmpty()
                ? conflictingWithFirst
                : conflictingWithFirst
                    .add(0, firstOfTopRanking);
    }

}
