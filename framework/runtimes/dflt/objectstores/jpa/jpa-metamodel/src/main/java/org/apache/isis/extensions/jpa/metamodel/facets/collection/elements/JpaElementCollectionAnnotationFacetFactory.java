package org.apache.isis.extensions.jpa.metamodel.facets.collection.elements;

import javax.persistence.ElementCollection;
import javax.persistence.FetchType;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;


public class JpaElementCollectionAnnotationFacetFactory extends
        AnnotationBasedFacetFactoryAbstract implements SpecificationLoaderAware {

    private SpecificationLoader specificationLoader;

    public JpaElementCollectionAnnotationFacetFactory() {
        super(FeatureType.COLLECTIONS_ONLY);
    }

    @Override
    public void process(ProcessMethodContext processMethodContext) {
        
        FacetHolder holder = processMethodContext.getFacetHolder();
        final ElementCollection annotation =
                getAnnotation(processMethodContext.getMethod(), ElementCollection.class);
        if (annotation == null) {
            return;
        } 
        
        final FetchType fetchType = annotation.fetch();
        final Class<?> targetElement = annotation.targetClass();
        
        FacetUtil
                .addFacet(
                new TypeOfFacetDerivedFromJpaElementCollectionAnnotation(
                        holder, targetElement, getSpecificationLoader()));
        FacetUtil
                .addFacet(new JpaFetchTypeFacetDerivedFromJpaElementCollectionsAnnotation(
                        holder, fetchType));
        FacetUtil.addFacet(new JpaElementsCollectionFacetAnnotation(
                holder));
    }
    
    private SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }
    @Override
    public void setSpecificationLoader(SpecificationLoader specificationLoader) {
        this.specificationLoader = specificationLoader;
    }


}
