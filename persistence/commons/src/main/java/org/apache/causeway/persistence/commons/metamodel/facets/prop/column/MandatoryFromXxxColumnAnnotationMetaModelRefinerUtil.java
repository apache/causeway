package org.apache.causeway.persistence.commons.metamodel.facets.prop.column;

import lombok.experimental.UtilityClass;

import org.apache.causeway.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

@UtilityClass
public class MandatoryFromXxxColumnAnnotationMetaModelRefinerUtil {
    public static void validateMandatoryFacet(final ObjectAssociation association) {

        association.lookupFacet(MandatoryFacet.class)
        .map(MandatoryFacet::getSharedFacetRankingElseFail)
        .ifPresent(facetRanking->facetRanking
                .visitTopRankPairsSemanticDiffering(MandatoryFacet.class, (a, b)->{

                    ValidationFailure.raiseFormatted(
                            association,
                            "%s: inconsistent Mandatory/Optional semantics specified in %s and %s.",
                            association.getFeatureIdentifier().toString(),
                            a.getClass().getSimpleName(),
                            b.getClass().getSimpleName());
                }));

    }
}
