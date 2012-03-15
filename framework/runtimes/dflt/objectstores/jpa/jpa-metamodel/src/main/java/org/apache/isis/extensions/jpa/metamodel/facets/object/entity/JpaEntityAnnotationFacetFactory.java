package org.apache.isis.extensions.jpa.metamodel.facets.object.entity;

import javax.persistence.Entity;

import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;


public class JpaEntityAnnotationFacetFactory extends
        AnnotationBasedFacetFactoryAbstract {

    public JpaEntityAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final Entity annotation = getAnnotation(cls, Entity.class);
        if (annotation == null) {
            return;
        }
        String annotationNameAttribute = annotation.name();
        if (StringUtils.isNullOrEmpty(annotationNameAttribute)) {
            annotationNameAttribute = cls.getSimpleName();
        }

        FacetUtil.addFacet(new JpaEntityFacetAnnotation(
                annotationNameAttribute, processClassContext.getFacetHolder()));
        return;
    }


}
