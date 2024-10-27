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
package org.apache.causeway.core.metamodel.facets.objectvalue.mandatory;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.functions._Predicates;
import org.apache.causeway.commons.internal.primitives._Ints;
import org.apache.causeway.commons.internal.primitives._Ints.Range;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.interactions.ActionArgValidityContext;
import org.apache.causeway.core.metamodel.interactions.PropertyModifyContext;
import org.apache.causeway.core.metamodel.interactions.ProposedHolder;
import org.apache.causeway.core.metamodel.interactions.ValidityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

import lombok.Getter;
import lombok.NonNull;

public abstract class MandatoryFacetAbstract
extends FacetAbstract
implements MandatoryFacet {

    private static final Class<? extends Facet> type() {
        return MandatoryFacet.class;
    }

    @Getter(onMethod_ = {@Override})
    private Semantics semantics;

    protected MandatoryFacetAbstract(final Semantics semantics, final FacetHolder holder) {
        this(semantics, holder, Precedence.DEFAULT);
    }

    protected MandatoryFacetAbstract(
            final Semantics semantics, final FacetHolder holder, final Facet.Precedence precedence) {
        super(type(), holder, precedence);
        this.semantics = semantics;
        if(!getSystemEnvironment().isUnitTesting()) {
            raiseIfConflictingOptionality(this);
        }
    }

    @Override
    public final boolean semanticEquals(final @NonNull Facet other) {
        return other instanceof MandatoryFacetAbstract
                ? this.getSemantics() == ((MandatoryFacetAbstract)other).getSemantics()
                : false;
    }

    /**
     * If not specified or, if a string, then zero length.
     */
    @Override
    public final boolean isRequiredButNull(final ManagedObject adapter) {
        if(getSemantics().isRequired()) {
            var pojo = MmUnwrapUtils.single(adapter);

            // special case string handling.
            if(pojo instanceof String) {
                return _Strings.isEmpty((String)pojo);
            }

            return pojo == null;
        } else {
            return false; // policy is not enforced
        }
    }

    @Override
    public String invalidates(final ValidityContext context) {

        var proposedHolder =
                context instanceof PropertyModifyContext
                || context instanceof ActionArgValidityContext
                        ? (ProposedHolder) context
                        : null;

        if(proposedHolder==null
                || !isRequiredButNull(proposedHolder.getProposed())) {
            return null;
        }

        return Optional.ofNullable(context.getFriendlyNameProvider())
        .map(Supplier::get)
        .filter(_Strings::isNotEmpty)
        .map(named->"'" + named + "' is mandatory")
        .orElse("Mandatory");
    }

    @Override
    public final void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("semantics", semantics);
    }

    // -- HELPER - VALIDATION STUFF

    private static final Range PRECEDENCE_ORDINALS_CONSIDERED_FOR_CONFLICTING_OPTIONALITY =
            _Ints.rangeClosed(Precedence.LOW.ordinal(), Precedence.HIGH.ordinal());

    /**
     * As called on the fly during {@link MandatoryFacet} creation,
     * not only checks the final top rank but also intermediate top ranks.
     * However, only considering {LOW, DEFAULT and HIGH}. Others are ignored.
     *
     * @apiNote as an alternative we could do a check only on the top rank once the MM is fully populated
     */
    private static void raiseIfConflictingOptionality(
            final @Nullable MandatoryFacet mandatoryFacet) {

        if(mandatoryFacet == null
                || !PRECEDENCE_ORDINALS_CONSIDERED_FOR_CONFLICTING_OPTIONALITY
                    .contains(mandatoryFacet.getPrecedence().ordinal())) {
            return; // ignore
        }

        final Can<MandatoryFacet> conflictingFacets = findConflictingMandatoryFacets(mandatoryFacet);

        if(conflictingFacets.isNotEmpty()) {
            var holder = mandatoryFacet.getFacetHolder();

            ValidationFailure.raiseFormatted(holder,
                    ProgrammingModelConstants.MessageTemplate.CONFLICTING_OPTIONALITY.builder()
                        .addVariable("member", holder.getFeatureIdentifier().getFullIdentityString())
                        .addVariable("conflictingFacets", conflictingFacets.stream()
                                .map(MandatoryFacet::summarize)
                                .collect(Collectors.joining(", \n")))
                        .buildMessage());
        }
    }

    private static Can<MandatoryFacet> findConflictingMandatoryFacets(final MandatoryFacet mandatoryFacet) {

        //TODO maybe move this kind of logic to FacetRanking

        var facetRanking = mandatoryFacet.getSharedFacetRanking().orElse(null);
        if(facetRanking==null) return Can.empty(); // not yet initialized

        // assumes that given mandatoryFacet is one of the top ranking

        var isTopRanking = mandatoryFacet.getPrecedence()
                .equals(facetRanking.getTopPrecedence().orElse(null));
        if(!isTopRanking) {
            // ignore validation of lower than top-rank
            return Can.empty();
        }

        var topRankingFacets = facetRanking.getTopRank(mandatoryFacet.facetType());
        var firstOfTopRanking = (MandatoryFacet)topRankingFacets.getFirstElseFail();

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
