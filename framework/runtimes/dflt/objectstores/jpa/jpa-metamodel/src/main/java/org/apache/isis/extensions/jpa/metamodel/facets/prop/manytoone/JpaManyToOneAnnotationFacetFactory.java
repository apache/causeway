package org.apache.isis.extensions.jpa.metamodel.facets.prop.manytoone;

import java.lang.reflect.Method;

import javax.persistence.ManyToOne;

import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;

public class JpaManyToOneAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public JpaManyToOneAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final ManyToOne annotation = getAnnotation(method, ManyToOne.class);
        if (annotation == null) {
            return;
        }
        final FacetedMethod holder = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new JpaManyToOneFacetAnnotation(holder));
        FacetUtil.addFacet(annotation.optional() ? new OptionalFacetDerivedFromJpaManyToOneAnnotation(holder) : new MandatoryFacetDerivedFromJpaManyToOneAnnotation(holder));
        FacetUtil.addFacet(new JpaFetchTypeFacetDerivedFromJpaManyToOneAnnotation(annotation.fetch(), holder));
    }

}
