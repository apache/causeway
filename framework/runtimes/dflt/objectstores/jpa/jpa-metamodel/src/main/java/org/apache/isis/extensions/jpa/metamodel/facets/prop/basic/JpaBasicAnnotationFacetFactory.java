package org.apache.isis.extensions.jpa.metamodel.facets.prop.basic;

import javax.persistence.Basic;

import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;


public class JpaBasicAnnotationFacetFactory extends
        AnnotationBasedFacetFactoryAbstract {

    public JpaBasicAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(ProcessMethodContext processMethodContext) {
        
        final Basic annotation = getAnnotation(processMethodContext.getMethod(), Basic.class);
        if (annotation == null) {
            return ;
        }
        
        final FacetedMethod holder = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new JpaBasicFacetAnnotation(holder));
        if (annotation.optional()) {
            // ie, a MandatoryFacet implementation with inverted semantics
            FacetUtil.addFacet(new OptionalFacetDerivedFromJpaBasicAnnotation(
                    holder));
        } else {
            FacetUtil.addFacet(new MandatoryFacetDerivedFromJpaBasicAnnotation(
                    holder));
        }
        FacetUtil.addFacet(new JpaFetchTypeFacetDerivedFromJpaBasicAnnotation(
                holder, annotation.fetch()));
    }


}
