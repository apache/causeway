package org.apache.causeway.persistence.commons.metamodel.facets.prop.column;

import lombok.experimental.UtilityClass;

import org.apache.causeway.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

@UtilityClass
public class MaxLengthFromXxxColumnAnnotationMetaModelRefinerUtil {
    public static void validateMaxLengthFacet(ObjectAssociation association) {
        association.lookupFacet(MaxLengthFacet.class)
        .map(MaxLengthFacet::getSharedFacetRankingElseFail)
        .ifPresent(facetRanking->facetRanking
                .visitTopRankPairsSemanticDiffering(MaxLengthFacet.class, (a, b)->{

                    ValidationFailure.raiseFormatted(
                            association,
                            "%s: inconsistent MaxLength semantics specified in %s and %s.",
                            association.getFeatureIdentifier().toString(),
                            a.getClass().getSimpleName(),
                            b.getClass().getSimpleName());
                }));
    }
}
