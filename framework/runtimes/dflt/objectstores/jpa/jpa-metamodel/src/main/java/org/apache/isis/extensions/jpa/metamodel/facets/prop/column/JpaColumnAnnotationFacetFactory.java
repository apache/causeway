package org.apache.isis.extensions.jpa.metamodel.facets.prop.column;

import javax.persistence.Column;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;

public class JpaColumnAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public JpaColumnAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(ProcessMethodContext processMethodContext) {
        final FacetedMethod holder = processMethodContext.getFacetHolder();
        final Column annotation = getAnnotation(processMethodContext.getMethod(), Column.class);
        if (annotation == null) {
            return;
        }
        FacetUtil.addFacet(new JpaColumnFacetAnnotation(annotation.name(), holder));
        final Facet facet = annotation.nullable() ? new OptionalFacetDerivedFromJpaColumnAnnotation(holder) : new MandatoryFacetDerivedFromJpaColumnAnnotation(holder);
        FacetUtil.addFacet(facet);
        FacetUtil.addFacet(new MaxLengthFacetDerivedFromJpaColumnAnnotation(annotation.length(), holder));
    }

}
