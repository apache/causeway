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

package org.apache.isis.metamodel.facets.properties.propertylayout;

import java.util.Optional;

import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.ContributeeMemberFacetFactory;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.FacetedMethod;
import org.apache.isis.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.metamodel.facets.objectvalue.labelat.LabelAtFacet;
import org.apache.isis.metamodel.facets.objectvalue.multiline.MultiLineFacet;
import org.apache.isis.metamodel.facets.objectvalue.renderedadjusted.RenderedAdjustedFacet;
import org.apache.isis.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;

import lombok.val;

public class PropertyLayoutFacetFactory extends FacetFactoryAbstract implements ContributeeMemberFacetFactory {

    public PropertyLayoutFacetFactory() {
        super(FeatureType.PROPERTIES_AND_ACTIONS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        val facetHolder = facetHolderFrom(processMethodContext);
        val propertyLayoutIfAny = propertyLayoutsFrom(processMethodContext);

        processCssClass(facetHolder, propertyLayoutIfAny);
        processDescribedAs(facetHolder, propertyLayoutIfAny);
        processPromptStyle(facetHolder, propertyLayoutIfAny);
        processHidden(facetHolder, propertyLayoutIfAny);
        processLabelAt(facetHolder, propertyLayoutIfAny);
        processMultiLine(facetHolder, propertyLayoutIfAny);
        processNamed(facetHolder, propertyLayoutIfAny);
        processRenderedAdjusted(facetHolder, propertyLayoutIfAny);
        processTypicalLength(facetHolder, propertyLayoutIfAny);
        processUnchanging(facetHolder, propertyLayoutIfAny);
    }

    void processCssClass(final FacetHolder holder, final Optional<PropertyLayout> propertyLayout) {
        val cssClassFacet = CssClassFacetForPropertyLayoutAnnotation
                .create(propertyLayout, holder);
        super.addFacet(cssClassFacet);
    }

    void processDescribedAs(
            final FacetHolder holder,
            final Optional<PropertyLayout> propertyLayout) {
        
        val describedAsFacet = DescribedAsFacetForPropertyLayoutAnnotation.create(propertyLayout, holder);
        super.addFacet(describedAsFacet);

    }

    void processPromptStyle(final FacetHolder holder, final Optional<PropertyLayout> propertyLayout) {

        if(holder instanceof FacetedMethod) {
            final FacetedMethod facetedMethod = (FacetedMethod) holder;
            if(facetedMethod.getFeatureType() != FeatureType.PROPERTY) {
                return;
            }
        }

        val promptStyleFacet = PromptStyleFacetForPropertyLayoutAnnotation
                .create(propertyLayout, getConfiguration(), holder);

        super.addFacet(promptStyleFacet);
    }

    void processHidden(final FacetHolder holder, final Optional<PropertyLayout> propertyLayout) {
        val hiddenFacet = HiddenFacetForPropertyLayoutAnnotation.create(propertyLayout, holder);
        super.addFacet(hiddenFacet);
    }

    void processLabelAt(
            final FacetHolder holder,
            final Optional<PropertyLayout> propertyLayout) {
        val labelAtFacet = LabelAtFacetForPropertyLayoutAnnotation.create(propertyLayout, holder);
        super.addFacet(labelAtFacet);
    }

    void processMultiLine(final FacetHolder holder, final Optional<PropertyLayout> propertyLayout) {
        val multiLineFacet = MultiLineFacetForPropertyLayoutAnnotation.create(propertyLayout, holder);
        super.addFacet(multiLineFacet);
    }

    void processNamed(final FacetHolder holder, final Optional<PropertyLayout> propertyLayout) {
        val namedFacet = NamedFacetForPropertyLayoutAnnotation.create(propertyLayout, holder);
        super.addFacet(namedFacet);
    }

    void processRenderedAdjusted(
            final FacetHolder holder,
            final Optional<PropertyLayout> propertyLayout) {
        val renderedAdjustedFacet = RenderedAdjustedFacetForPropertyLayoutAnnotation
                .create(propertyLayout, holder);
        super.addFacet(renderedAdjustedFacet);
    }

    void processTypicalLength(
            final FacetHolder holder,
            final Optional<PropertyLayout> propertyLayout) {
        val typicalLengthFacet = TypicalLengthFacetForPropertyLayoutAnnotation
                .create(propertyLayout, holder);
        super.addFacet(typicalLengthFacet);
    }

    void processUnchanging(
            final FacetHolder holder,
            final Optional<PropertyLayout> propertyLayout) {
        val unchangingFacet = UnchangingFacetForPropertyLayoutAnnotation.create(propertyLayout, holder);
        super.addFacet(unchangingFacet);
    }

    @Override
    public void process(ProcessContributeeMemberContext processMemberContext) {

        // cssClass
        CssClassFacet cssClassFacet = null;
        super.addFacet(cssClassFacet);


        // describedAs
        DescribedAsFacet describedAsFacet = null;
        super.addFacet(describedAsFacet);


        // hidden
        HiddenFacet hiddenFacet = null;
        super.addFacet(hiddenFacet);


        // labelAt
        LabelAtFacet labelAtFacet = null;
        super.addFacet(labelAtFacet);


        // multiLine
        MultiLineFacet multiLineFacet = null;
        super.addFacet(multiLineFacet);


        // named
        NamedFacet namedFacet = null;
        super.addFacet(namedFacet);


        // renderedAsDayBefore
        RenderedAdjustedFacet renderedAdjustedFacet = null;
        super.addFacet(renderedAdjustedFacet);


        // typicalLength
        TypicalLengthFacet typicalLengthFacet = null;
        super.addFacet(typicalLengthFacet);

    }


    FacetedMethod facetHolderFrom(final ProcessMethodContext processMethodContext) {
        return processMethodContext.getFacetHolder();
    }

    Optional<PropertyLayout> propertyLayoutsFrom(final ProcessMethodContext processMethodContext) {
        return processMethodContext.synthesizeOnMethodOrMixinType(PropertyLayout.class);
    }


}
