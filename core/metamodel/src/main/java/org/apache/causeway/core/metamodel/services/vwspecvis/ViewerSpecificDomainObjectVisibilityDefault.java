package org.apache.causeway.core.metamodel.services.vwspecvis;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;

import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.viewer.web.ViewerSpecificDomainObjectVisibility;

@Service
public record ViewerSpecificDomainObjectVisibilityDefault(
        CausewayConfiguration config)
implements ViewerSpecificDomainObjectVisibility {

    @Override
    public boolean isVisibleFor(final LogicalType logicalType, final String viewerId) {
        if(logicalType==null || !StringUtils.hasLength(viewerId))
            return true; // don't veto null type nor missing viewerId

        var mergedAnnotations = _ClassCache.getInstance()
            .head(logicalType.correspondingClass())
            .mergedAnnotations();

        var profilesSupportedByViewer = config.viewerProfiles().profilesForViewer(viewerId);
        var profilesEnabledForShowing =
            showFor(mergedAnnotations, org.apache.causeway.applib.annotation.DomainObject.class)
            .or(()->showFor(mergedAnnotations, org.apache.causeway.applib.annotation.DomainService.class))
            .orElseGet(()->config.viewerProfiles().profilesForNamespace(logicalType.namespace()));

        //TODO optimization: perhaps do this check without creating a throw-away set
        return !_Sets.intersect(profilesSupportedByViewer, profilesEnabledForShowing)
            .isEmpty();
    }

    // -- HELPER

    private <A extends Annotation> Optional<Set<String>> showFor(
            final MergedAnnotations mergedAnnotations,
            final Class<A> annotationType) {
        //TODO benchmark: how fast is this?
        var annot = mergedAnnotations.get(annotationType);
        return annot.isPresent()
                && annot.hasNonDefaultValue("showFor")
            ? Optional.ofNullable(annot.getStringArray("showFor"))
                    .map(Set::of)
            : Optional.empty();
    }

}
