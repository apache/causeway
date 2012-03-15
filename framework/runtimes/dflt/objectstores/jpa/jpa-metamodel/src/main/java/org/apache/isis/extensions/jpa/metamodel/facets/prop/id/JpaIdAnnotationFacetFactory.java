package org.apache.isis.extensions.jpa.metamodel.facets.prop.id;

import javax.persistence.Id;

import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;


public class JpaIdAnnotationFacetFactory extends
        AnnotationBasedFacetFactoryAbstract {

    public JpaIdAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(ProcessMethodContext processMethodContext) {
        final Id annotation = getAnnotation(processMethodContext.getMethod(), Id.class);
        if (annotation == null) {
            return;
        }

        final FacetedMethod holder = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new JpaIdFacetAnnotation(holder));
        FacetUtil.addFacet(new OptionalFacetDerivedFromJpaIdAnnotation(holder));
        FacetUtil.addFacet(new DisabledFacetDerivedFromJpaIdAnnotation(holder));
    }

}
