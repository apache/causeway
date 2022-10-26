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

import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MetaModelValidatorForConflictingOptionality {

    // assumes that given mandatoryFacet is one of the top ranking
    public static void flagIfConflict(final MandatoryFacet mandatoryFacet, final String message) {

        if(false && //FIXME yet has false positives

                conflictingOptionality(mandatoryFacet)) {
            addFailure(mandatoryFacet, message);
        }
    }

    // -- HELPER

    private static void addFailure(final MandatoryFacet mandatoryFacet, final String message) {
        if(mandatoryFacet != null) {
            val holder = mandatoryFacet.getFacetHolder();
            ValidationFailure.raiseFormatted(
                    holder,
                    "%s : %s",
                    message,
                    holder.getFeatureIdentifier().getFullIdentityString());
        }
    }

    private static boolean conflictingOptionality(final MandatoryFacet mandatoryFacet) {
        if (mandatoryFacet == null) {
            return false;
        }

        //TODO maybe move this kind of logic to FacetRanking

        val facetRanking = mandatoryFacet.getSharedFacetRankingElseFail();

        // assumes that given mandatoryFacet is one of the top ranking
        _Assert.assertEquals(
                mandatoryFacet.getPrecedence(),
                facetRanking.getTopPrecedence().orElse(null));

        val topRankingFacets = facetRanking.getTopRank(mandatoryFacet.facetType());
        val firstOfTopRanking = topRankingFacets.getFirstOrFail();

        // the top ranking mandatory facets should semantically agree

        return topRankingFacets.isCardinalityMultiple()
                ? topRankingFacets
                        .stream()
                        .skip(1)
                        .anyMatch(firstOfTopRanking::semanticEquals)
                : false; // not conflicting

    }




}
