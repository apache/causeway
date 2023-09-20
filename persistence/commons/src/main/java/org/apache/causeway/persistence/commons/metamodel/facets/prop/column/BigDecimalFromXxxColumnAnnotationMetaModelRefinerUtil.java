package org.apache.causeway.persistence.commons.metamodel.facets.prop.column;

import lombok.experimental.UtilityClass;

import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MaxFractionalDigitsFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MaxTotalDigitsFacet;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

@UtilityClass
public class BigDecimalFromXxxColumnAnnotationMetaModelRefinerUtil {

    public static void validateBigDecimalValueFacet(final ObjectAssociation association) {

        association.lookupFacet(MaxTotalDigitsFacet.class)
        .map(MaxTotalDigitsFacet::getSharedFacetRankingElseFail)
        .ifPresent(facetRanking->facetRanking
                .visitTopRankPairsSemanticDiffering(MaxTotalDigitsFacet.class, (a, b)->{

                    ValidationFailure.raiseFormatted(
                            association,
                            "%s: inconsistent MaxTotalDigits semantics specified in %s and %s.",
                            association.getFeatureIdentifier().toString(),
                            a.getClass().getSimpleName(),
                            b.getClass().getSimpleName());
                }));

        association.lookupFacet(MaxFractionalDigitsFacet.class)
        .map(MaxFractionalDigitsFacet::getSharedFacetRankingElseFail)
        .ifPresent(facetRanking->facetRanking
                .visitTopRankPairsSemanticDiffering(MaxFractionalDigitsFacet.class, (a, b)->{

                    ValidationFailure.raiseFormatted(
                            association,
                            "%s: inconsistent MaxFractionalDigits semantics specified in %s and %s.",
                            association.getFeatureIdentifier().toString(),
                            a.getClass().getSimpleName(),
                            b.getClass().getSimpleName());
                }));
    }
}
