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

import java.lang.reflect.Method;
import java.util.Properties;

import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.ContributeeMemberFacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.members.order.annotprop.MemberOrderFacetAnnotation;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.labelat.LabelAtFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.multiline.MultiLineFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.renderedadjusted.RenderedAdjustedFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.facets.properties.renderunchanged.UnchangingFacet;
import org.datanucleus.util.StringUtils;

public class PropertyLayoutFacetFactory extends FacetFactoryAbstract implements ContributeeMemberFacetFactory {

    public PropertyLayoutFacetFactory() {
        super(FeatureType.PROPERTIES_AND_ACTIONS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final FacetHolder holder = facetHolderFrom(processMethodContext);
        final Properties properties = metadataPropertiesFrom(processMethodContext);
        final PropertyLayout propertyLayout = propertyLayoutAnnotationFrom(processMethodContext);

        processCssClass(holder, properties, propertyLayout);

        processDescribedAs(holder, properties, propertyLayout);

        processPromptStyle(holder, properties, propertyLayout);

        processHidden(holder, properties, propertyLayout);

        processLabelAt(holder, properties, propertyLayout);

        processMultiLine(holder, properties, propertyLayout);

        processNamed(holder, properties, propertyLayout);

        processRenderedAdjusted(holder, properties, propertyLayout);

        processTypicalLength(holder, properties, propertyLayout);

        processUnchanging(holder, properties, propertyLayout);

        // In preparation for v2 adding support for sequence in @PropertyLayout
        if (propertyLayout!=null
                && StringUtils.notEmpty(propertyLayout.sequence())
                && holder.getFacet(MemberOrderFacetAnnotation.class)==null) {
            FacetUtil.addFacet( new MemberOrderFacetAnnotation(
                    "__infer".equals( propertyLayout.fieldSetName()) ? "" : propertyLayout.fieldSetName(),
                    propertyLayout.sequence(),
                    servicesInjector.lookupService(TranslationService.class),
                    holder));
        }
    }

    void processCssClass(final FacetHolder holder, final Properties properties, final PropertyLayout propertyLayout) {
        CssClassFacet cssClassFacet = CssClassFacetOnPropertyFromLayoutProperties.create(properties, holder);
        if(cssClassFacet == null) {
            cssClassFacet = CssClassFacetForPropertyLayoutAnnotation.create(propertyLayout, holder);
        }
        FacetUtil.addFacet(cssClassFacet);
    }

    void processDescribedAs(
            final FacetHolder holder,
            final Properties properties,
            final PropertyLayout propertyLayout) {
        DescribedAsFacet describedAsFacet = DescribedAsFacetOnPropertyFromLayoutProperties.create(properties, holder);
        if(describedAsFacet == null) {
            describedAsFacet = DescribedAsFacetForPropertyLayoutAnnotation.create(propertyLayout, holder);
        }
        FacetUtil.addFacet(describedAsFacet);
    }

    void processPromptStyle(final FacetHolder holder, final Properties properties, final PropertyLayout propertyLayout) {

        if(holder instanceof FacetedMethod) {
            final FacetedMethod facetedMethod = (FacetedMethod) holder;
            if(facetedMethod.getFeatureType() != FeatureType.PROPERTY) {
                return;
            }
        }

        PromptStyleFacet promptStyleFacet = PromptStyleFacetOnPropertyFromLayoutProperties
                .create(properties, holder);
        if(promptStyleFacet == null) {
            promptStyleFacet = PromptStyleFacetForPropertyLayoutAnnotation
                    .create(propertyLayout, getConfiguration(), holder);
        }

        FacetUtil.addFacet(promptStyleFacet);
    }

    void processHidden(final FacetHolder holder, final Properties properties, final PropertyLayout propertyLayout) {
        HiddenFacet hiddenFacet = HiddenFacetOnPropertyFromLayoutProperties.create(properties, holder);
        if(hiddenFacet == null) {
            hiddenFacet = HiddenFacetForPropertyLayoutAnnotation.create(propertyLayout, holder);
        }
        FacetUtil.addFacet(hiddenFacet);
    }

    void processLabelAt(
            final FacetHolder holder,
            final Properties properties,
            final PropertyLayout propertyLayout) {
        LabelAtFacet labelAtFacet = LabelAtFacetOnPropertyFromLayoutProperties.create(properties, holder);
        if(labelAtFacet == null) {
            labelAtFacet = LabelAtFacetForPropertyLayoutAnnotation.create(propertyLayout, holder);
        }
        FacetUtil.addFacet(labelAtFacet);
    }

    void processMultiLine(final FacetHolder holder, final Properties properties, final PropertyLayout propertyLayout) {
        MultiLineFacet multiLineFacet = MultiLineFacetOnPropertyFromLayoutProperties.create(properties, holder);
        if(multiLineFacet == null) {
            multiLineFacet = MultiLineFacetForPropertyLayoutAnnotation.create(propertyLayout, holder);
        }
        FacetUtil.addFacet(multiLineFacet);
    }

    void processNamed(final FacetHolder holder, final Properties properties, final PropertyLayout propertyLayout) {
        NamedFacet namedFacet = NamedFacetOnPropertyFromLayoutProperties.create(properties, holder);
        if(namedFacet == null) {
            namedFacet = NamedFacetForPropertyLayoutAnnotation.create(propertyLayout, holder);
        }
        FacetUtil.addFacet(namedFacet);
    }

    void processRenderedAdjusted(
            final FacetHolder holder,
            final Properties properties,
            final PropertyLayout propertyLayout) {
        RenderedAdjustedFacet renderedAdjustedFacet = RenderedAdjustedFacetOnPropertyFromLayoutProperties
                .create(properties, holder);
        if(renderedAdjustedFacet == null) {
            renderedAdjustedFacet = RenderedAdjustedFacetForPropertyLayoutAnnotation.create(propertyLayout, holder);
        }
        FacetUtil.addFacet(renderedAdjustedFacet);
    }

    void processTypicalLength(
            final FacetHolder holder,
            final Properties properties,
            final PropertyLayout propertyLayout) {
        TypicalLengthFacet typicalLengthFacet = TypicalLengthFacetOnPropertyFromLayoutProperties
                .create(properties, holder);
        if(typicalLengthFacet == null) {
            typicalLengthFacet = TypicalLengthFacetForPropertyLayoutAnnotation.create(propertyLayout, holder);
        }
        FacetUtil.addFacet(typicalLengthFacet);
    }

    void processUnchanging(
            final FacetHolder holder,
            final Properties properties,
            final PropertyLayout propertyLayout) {
        UnchangingFacet unchangingFacet = UnchangingFacetOnPropertyFromLayoutProperties
                .create(properties, holder);
        if(unchangingFacet == null) {
            unchangingFacet = UnchangingFacetForPropertyLayoutAnnotation.create(propertyLayout, holder);
        }
        FacetUtil.addFacet(unchangingFacet);
    }

    @Override
    public void process(ProcessContributeeMemberContext processMemberContext) {
        final FacetHolder holder = processMemberContext.getFacetHolder();

        Properties properties = metadataPropertiesFrom(processMemberContext);


        // cssClass
        CssClassFacet cssClassFacet = CssClassFacetOnPropertyFromLayoutProperties.create(properties, holder);
        FacetUtil.addFacet(cssClassFacet);


        // describedAs
        DescribedAsFacet describedAsFacet = DescribedAsFacetOnPropertyFromLayoutProperties.create(properties, holder);
        FacetUtil.addFacet(describedAsFacet);


        // hidden
        HiddenFacet hiddenFacet = HiddenFacetOnPropertyFromLayoutProperties.create(properties, holder);
        FacetUtil.addFacet(hiddenFacet);


        // labelAt
        LabelAtFacet labelAtFacet = LabelAtFacetOnPropertyFromLayoutProperties.create(properties, holder);
        FacetUtil.addFacet(labelAtFacet);


        // multiLine
        MultiLineFacet multiLineFacet = MultiLineFacetOnPropertyFromLayoutProperties.create(properties, holder);
        FacetUtil.addFacet(multiLineFacet);


        // named
        NamedFacet namedFacet = NamedFacetOnPropertyFromLayoutProperties.create(properties, holder);
        FacetUtil.addFacet(namedFacet);


        // renderedAsDayBefore
        RenderedAdjustedFacet renderedAdjustedFacet = RenderedAdjustedFacetOnPropertyFromLayoutProperties.create(properties, holder);
        FacetUtil.addFacet(renderedAdjustedFacet);


        // typicalLength
        TypicalLengthFacet typicalLengthFacet = TypicalLengthFacetOnPropertyFromLayoutProperties.create(properties, holder);
        FacetUtil.addFacet(typicalLengthFacet);

    }

    Properties metadataPropertiesFrom(final ProcessMethodContext processMethodContext) {
        Properties properties = processMethodContext.metadataProperties("propertyLayout");
        if(properties == null) {
            // alternate key
            properties = processMethodContext.metadataProperties("layout");
        }
        return properties;
    }

    FacetedMethod facetHolderFrom(final ProcessMethodContext processMethodContext) {
        return processMethodContext.getFacetHolder();
    }

    PropertyLayout propertyLayoutAnnotationFrom(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        return Annotations.getAnnotation(method, PropertyLayout.class);
    }


    Properties metadataPropertiesFrom(final ProcessContributeeMemberContext processMemberContext) {
        Properties properties = processMemberContext.metadataProperties("propertyLayout");
        if(properties == null) {
            // alternate key
            properties = processMemberContext.metadataProperties("layout");
        }
        return properties;
    }

}
