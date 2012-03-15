package org.apache.isis.extensions.jpa.metamodel.facets.prop.onetoone;

import java.lang.reflect.Method;

import javax.persistence.OneToOne;

import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;

public class JpaOneToOneAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public JpaOneToOneAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final OneToOne annotation = getAnnotation(method, OneToOne.class);
        if (annotation == null) {
            return;
        }
        final FacetedMethod holder = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new JpaOneToOneFacetAnnotation(holder));
        FacetUtil.addFacet(annotation.optional() ? new OptionalFacetDerivedFromJpaOneToOneAnnotation(holder) : new MandatoryFacetDerivedFromJpaOneToOneAnnotation(holder));
        FacetUtil.addFacet(new JpaFetchTypeFacetDerivedFromJpaOneToOneAnnotation(annotation.fetch(), holder));
    }

}
