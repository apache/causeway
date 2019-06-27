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

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.Annotations;
import org.apache.isis.metamodel.facets.ContributeeMemberFacetFactory;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.FacetedMethod;
import org.apache.isis.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.metamodel.facets.objectvalue.labelat.LabelAtFacet;
import org.apache.isis.metamodel.facets.objectvalue.multiline.MultiLineFacet;
import org.apache.isis.metamodel.facets.objectvalue.renderedadjusted.RenderedAdjustedFacet;
import org.apache.isis.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.metamodel.facets.properties.renderunchanged.UnchangingFacet;

public class PropertyLayoutFacetFactory extends FacetFactoryAbstract implements ContributeeMemberFacetFactory {

    public PropertyLayoutFacetFactory() {
        super(FeatureType.PROPERTIES_AND_ACTIONS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final FacetHolder holder = facetHolderFrom(processMethodContext);
        final List<PropertyLayout> propertyLayouts = propertyLayoutsFrom(processMethodContext);

        processCssClass(holder, propertyLayouts);

        processDescribedAs(holder, propertyLayouts);

        processPromptStyle(holder, propertyLayouts);

        processHidden(holder, propertyLayouts);

        processLabelAt(holder, propertyLayouts);

        processMultiLine(holder, propertyLayouts);

        processNamed(holder, propertyLayouts);

        processRenderedAdjusted(holder, propertyLayouts);

        processTypicalLength(holder, propertyLayouts);

        processUnchanging(holder, propertyLayouts);
    }

    void processCssClass(final FacetHolder holder, final List<PropertyLayout> propertyLayout) {
        CssClassFacet cssClassFacet = CssClassFacetForPropertyLayoutAnnotation.create(propertyLayout, holder);
        FacetUtil.addFacet(cssClassFacet);
    }

    void processDescribedAs(
            final FacetHolder holder,
            final List<PropertyLayout> propertyLayout) {
        DescribedAsFacet describedAsFacet = DescribedAsFacetForPropertyLayoutAnnotation.create(propertyLayout, holder);
        FacetUtil.addFacet(describedAsFacet);
    }

    void processPromptStyle(final FacetHolder holder, final List<PropertyLayout> propertyLayout) {

        if(holder instanceof FacetedMethod) {
            final FacetedMethod facetedMethod = (FacetedMethod) holder;
            if(facetedMethod.getFeatureType() != FeatureType.PROPERTY) {
                return;
            }
        }

        PromptStyleFacet promptStyleFacet = PromptStyleFacetForPropertyLayoutAnnotation
                .create(propertyLayout, getConfiguration(), holder);

        FacetUtil.addFacet(promptStyleFacet);
    }

    void processHidden(final FacetHolder holder, final List<PropertyLayout> propertyLayout) {
        HiddenFacet hiddenFacet = HiddenFacetForPropertyLayoutAnnotation.create(propertyLayout, holder);
        FacetUtil.addFacet(hiddenFacet);
    }

    void processLabelAt(
            final FacetHolder holder,
            final List<PropertyLayout> propertyLayout) {
        LabelAtFacet labelAtFacet = LabelAtFacetForPropertyLayoutAnnotation.create(propertyLayout, holder);
        FacetUtil.addFacet(labelAtFacet);
    }

    void processMultiLine(final FacetHolder holder, final List<PropertyLayout> propertyLayout) {
        MultiLineFacet multiLineFacet = MultiLineFacetForPropertyLayoutAnnotation.create(propertyLayout, holder);
        FacetUtil.addFacet(multiLineFacet);
    }

    void processNamed(final FacetHolder holder, final List<PropertyLayout> propertyLayout) {
        NamedFacet namedFacet = NamedFacetForPropertyLayoutAnnotation.create(propertyLayout, holder);
        FacetUtil.addFacet(namedFacet);
    }

    void processRenderedAdjusted(
            final FacetHolder holder,
            final List<PropertyLayout> propertyLayout) {
        RenderedAdjustedFacet renderedAdjustedFacet = RenderedAdjustedFacetForPropertyLayoutAnnotation
                .create(propertyLayout, holder);
        FacetUtil.addFacet(renderedAdjustedFacet);
    }

    void processTypicalLength(
            final FacetHolder holder,
            final List<PropertyLayout> propertyLayout) {
        TypicalLengthFacet typicalLengthFacet = TypicalLengthFacetForPropertyLayoutAnnotation
                .create(propertyLayout, holder);
        FacetUtil.addFacet(typicalLengthFacet);
    }

    void processUnchanging(
            final FacetHolder holder,
            final List<PropertyLayout> propertyLayout) {
        UnchangingFacet unchangingFacet = UnchangingFacetForPropertyLayoutAnnotation.create(propertyLayout, holder);
        FacetUtil.addFacet(unchangingFacet);
    }

    @Override
    public void process(ProcessContributeeMemberContext processMemberContext) {

        // cssClass
        CssClassFacet cssClassFacet = null;
        FacetUtil.addFacet(cssClassFacet);


        // describedAs
        DescribedAsFacet describedAsFacet = null;
        FacetUtil.addFacet(describedAsFacet);


        // hidden
        HiddenFacet hiddenFacet = null;
        FacetUtil.addFacet(hiddenFacet);


        // labelAt
        LabelAtFacet labelAtFacet = null;
        FacetUtil.addFacet(labelAtFacet);


        // multiLine
        MultiLineFacet multiLineFacet = null;
        FacetUtil.addFacet(multiLineFacet);


        // named
        NamedFacet namedFacet = null;
        FacetUtil.addFacet(namedFacet);


        // renderedAsDayBefore
        RenderedAdjustedFacet renderedAdjustedFacet = null;
        FacetUtil.addFacet(renderedAdjustedFacet);


        // typicalLength
        TypicalLengthFacet typicalLengthFacet = null;
        FacetUtil.addFacet(typicalLengthFacet);

    }


    FacetedMethod facetHolderFrom(final ProcessMethodContext processMethodContext) {
        return processMethodContext.getFacetHolder();
    }

    List<PropertyLayout> propertyLayoutsFrom(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        return Annotations.getAnnotations(method, PropertyLayout.class);
    }


}
