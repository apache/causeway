/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

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
