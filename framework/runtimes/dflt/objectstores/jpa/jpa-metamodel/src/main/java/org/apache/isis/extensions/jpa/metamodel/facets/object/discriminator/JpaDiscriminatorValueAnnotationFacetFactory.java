package org.apache.isis.extensions.jpa.metamodel.facets.object.discriminator;

import javax.persistence.DiscriminatorValue;

import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;


public class JpaDiscriminatorValueAnnotationFacetFactory extends
        AnnotationBasedFacetFactoryAbstract {

    public JpaDiscriminatorValueAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }


    @Override
    public void process(ProcessClassContext processClassContext) {

        final DiscriminatorValue annotation = getAnnotation(processClassContext.getCls(),
                DiscriminatorValue.class);
        if (annotation == null) {
            return;
        }
        final String annotationValueAttribute = annotation.value();

        FacetUtil.addFacet(new ObjectTypeFacetInferredFromJpaDiscriminatorValueAnnotation(
                annotationValueAttribute, processClassContext.getFacetHolder()));
    }


}
