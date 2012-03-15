package org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;


public class JpaNamedQueryAnnotationFacetFactory extends
        AnnotationBasedFacetFactoryAbstract {

    public JpaNamedQueryAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final NamedQueries namedQueriesAnnotation = getAnnotation(cls,
                NamedQueries.class);
        final FacetHolder facetHolder = processClassContext.getFacetHolder();
        
        if (namedQueriesAnnotation != null) {
            FacetUtil.addFacet(new JpaNamedQueriesFacetAnnotation(
                    namedQueriesAnnotation.value(), facetHolder));
            return;
        }

        final NamedQuery namedQueryAnnotation = getAnnotation(cls,
                NamedQuery.class);
        if (namedQueryAnnotation != null) {
            FacetUtil.addFacet(new JpaNamedQueryFacetAnnotation(
                    namedQueryAnnotation, facetHolder));
        }
    }
}
