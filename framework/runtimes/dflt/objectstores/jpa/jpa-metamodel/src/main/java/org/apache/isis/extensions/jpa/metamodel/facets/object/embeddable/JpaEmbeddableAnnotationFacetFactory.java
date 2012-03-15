package org.apache.isis.extensions.jpa.metamodel.facets.object.embeddable;

import javax.persistence.Embeddable;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;


public class JpaEmbeddableAnnotationFacetFactory extends
        AnnotationBasedFacetFactoryAbstract {

    public JpaEmbeddableAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(ProcessClassContext processMethodContext) {
        final Embeddable annotation = getAnnotation(processMethodContext.getCls(), Embeddable.class);
        if (annotation == null) {
            return;
        }

        final FacetHolder facetHolder = processMethodContext.getFacetHolder();
        FacetUtil
                .addFacet(new AggregatedFacetDerivedFromJpaEmbeddableAnnotation(
                        facetHolder));
        FacetUtil.addFacet(new JpaEmbeddableFacetAnnotation(facetHolder));

        return;
    }


}
