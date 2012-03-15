package org.apache.isis.extensions.jpa.metamodel.facets.prop.transience;

import javax.persistence.Transient;

import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;

public class JpaTransientAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public JpaTransientAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(ProcessMethodContext processMethodContext) {
        final Transient annotation = getAnnotation(processMethodContext.getMethod(), Transient.class);
        if (annotation == null) {
            return;
        }
        final FacetedMethod holder = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new JpaTransientFacetAnnotation(holder));
        FacetUtil.addFacet(new DerivedFacetDerivedFromJpaTransientAnnotation(holder));
    }

}
