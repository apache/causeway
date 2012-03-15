package org.apache.isis.extensions.jpa.metamodel.facets.prop.version;

import javax.persistence.Version;

import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;

public class JpaVersionAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public JpaVersionAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(ProcessMethodContext processMethodContext) {
        final Version annotation = getAnnotation(processMethodContext.getMethod(), Version.class);
        if (annotation == null) {
            return;
        }
        final FacetedMethod holder = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new JpaVersionFacetAnnotation(holder));
        FacetUtil.addFacet(new DisabledFacetDerivedFromJpaVersionAnnotation(holder));
        FacetUtil.addFacet(new OptionalFacetDerivedFromJpaVersionAnnotation(holder));
    }

}
