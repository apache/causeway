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

package org.apache.isis.core.metamodel.facets.properties.propertylayout;

import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.members.layout.group.LayoutGroupFacetFromPropertyLayoutAnnotation;
import org.apache.isis.core.metamodel.facets.members.layout.order.LayoutOrderFacetFromPropertyLayoutAnnotation;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForAmbiguousMixinAnnotations;

import lombok.val;

public class PropertyLayoutFacetFactory 
extends FacetFactoryAbstract {
    
    public PropertyLayoutFacetFactory() {
        super(FeatureType.PROPERTIES_AND_ACTIONS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        val facetHolder = processMethodContext.getFacetHolder();
        val propertyLayoutIfAny = processMethodContext
                .synthesizeOnMethodOrMixinType(
                        PropertyLayout.class, 
                        () -> MetaModelValidatorForAmbiguousMixinAnnotations
                        .addValidationFailure(processMethodContext.getFacetHolder(), PropertyLayout.class));

        val cssClassFacet = CssClassFacetForPropertyLayoutAnnotation
                .create(propertyLayoutIfAny, facetHolder);
        super.addFacet(cssClassFacet);
        
        val describedAsFacet = DescribedAsFacetForPropertyLayoutAnnotation
                .create(propertyLayoutIfAny, facetHolder);
        super.addFacet(describedAsFacet);

        val hiddenFacet = HiddenFacetForPropertyLayoutAnnotation
                .create(propertyLayoutIfAny, facetHolder);
        super.addFacet(hiddenFacet);
        
        val labelAtFacet = LabelAtFacetForPropertyLayoutAnnotation
                .create(propertyLayoutIfAny, facetHolder);
        super.addFacet(labelAtFacet);
        
        val layoutGroupFacet = LayoutGroupFacetFromPropertyLayoutAnnotation
                .create(propertyLayoutIfAny, facetHolder);
        super.addFacet(layoutGroupFacet);
        
        val layoutOrderFacet = LayoutOrderFacetFromPropertyLayoutAnnotation
                .create(propertyLayoutIfAny, facetHolder);
        super.addFacet(layoutOrderFacet);
        
        val multiLineFacet = MultiLineFacetForPropertyLayoutAnnotation
                .create(propertyLayoutIfAny, facetHolder);
        super.addFacet(multiLineFacet);
        
        val namedFacet = NamedFacetForPropertyLayoutAnnotation
                .create(propertyLayoutIfAny, facetHolder);
        super.addFacet(namedFacet);
        
        val promptStyleFacet = PromptStyleFacetForPropertyLayoutAnnotation
                .create(propertyLayoutIfAny, getConfiguration(), facetHolder);
        super.addFacet(promptStyleFacet);
        
        val renderedAdjustedFacet = RenderedAdjustedFacetForPropertyLayoutAnnotation
                .create(propertyLayoutIfAny, facetHolder);
        super.addFacet(renderedAdjustedFacet);
        
        val typicalLengthFacet = TypicalLengthFacetForPropertyLayoutAnnotation
                .create(propertyLayoutIfAny, facetHolder);
        super.addFacet(typicalLengthFacet);
        
        val unchangingFacet = UnchangingFacetForPropertyLayoutAnnotation
                .create(propertyLayoutIfAny, facetHolder);
        super.addFacet(unchangingFacet);
        
    }
    

}
