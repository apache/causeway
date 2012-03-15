package org.apache.isis.extensions.jpa.metamodel.facets.prop.joincolumn;

import java.lang.reflect.Method;

import javax.persistence.JoinColumn;

import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;

public class JpaJoinColumnAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public JpaJoinColumnAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final JoinColumn annotation = getAnnotation(method, JoinColumn.class);
        if (annotation == null) {
            return;
        }
        final FacetedMethod holder = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new JpaJoinColumnFacetAnnotation(annotation.name(), holder));
        FacetUtil.addFacet(annotation.nullable() ? new OptionalFacetDerivedFromJpaJoinColumnAnnotation(holder) : new MandatoryFacetDerivedFromJpaJoinColumnAnnotation(holder));
        return;
    }

}
