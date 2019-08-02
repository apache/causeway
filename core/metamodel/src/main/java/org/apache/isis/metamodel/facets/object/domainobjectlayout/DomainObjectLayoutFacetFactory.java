/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */
package org.apache.isis.metamodel.facets.object.domainobjectlayout;

import java.util.List;

import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.ViewModelLayout;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.Annotations;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.services.events.MetamodelEventService;

import lombok.val;

public class DomainObjectLayoutFacetFactory extends FacetFactoryAbstract {


    public DomainObjectLayoutFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();

        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final List<DomainObjectLayout> domainObjectLayouts = Annotations.getAnnotations(cls, DomainObjectLayout.class);
        final List<ViewModelLayout> viewModelLayouts = Annotations.getAnnotations(cls, ViewModelLayout.class);

        val metamodelEventService = getServiceRegistry().lookupServiceElseFail(MetamodelEventService.class);

        FacetUtil.addFacet(
                TitleFacetViaDomainObjectLayoutAnnotationUsingTitleUiEvent.create(
                        domainObjectLayouts, metamodelEventService, getConfiguration(), facetHolder));
        FacetUtil.addFacet(
                TitleFacetViaViewModelLayoutAnnotationUsingTitleUiEvent.create(
                        viewModelLayouts, metamodelEventService, getConfiguration(), facetHolder));

        FacetUtil.addFacet(
                IconFacetViaDomainObjectLayoutAnnotationUsingIconUiEvent.create(
                        domainObjectLayouts, metamodelEventService, getConfiguration(), facetHolder));
        FacetUtil.addFacet(
                IconFacetViaViewModelLayoutAnnotationUsingIconUiEvent.create(
                        viewModelLayouts, metamodelEventService, getConfiguration(), facetHolder));

        FacetUtil.addFacet(
                CssClassFacetViaDomainObjectLayoutAnnotationUsingCssClassUiEvent.create(
                        domainObjectLayouts, metamodelEventService, getConfiguration(), facetHolder));
        FacetUtil.addFacet(
                CssClassFacetViaViewModelLayoutAnnotationUsingCssClassUiEvent.create(
                        viewModelLayouts, metamodelEventService, getConfiguration(), facetHolder));

        FacetUtil.addFacet(
                LayoutFacetViaDomainObjectLayoutAnnotationUsingLayoutUiEvent.create(
                        domainObjectLayouts, metamodelEventService, getConfiguration(), facetHolder));
        FacetUtil.addFacet(
                LayoutFacetViaViewModelLayoutAnnotationUsingLayoutUiEvent.create(
                        viewModelLayouts, metamodelEventService, getConfiguration(), facetHolder));

        FacetUtil.addFacet(
                CssClassFacetForDomainObjectLayoutAnnotation.create(domainObjectLayouts, facetHolder));
        FacetUtil.addFacet(
                CssClassFacetForViewModelLayoutAnnotation.create(viewModelLayouts, facetHolder));

        FacetUtil.addFacet(
                CssClassFaFacetForDomainObjectLayoutAnnotation.create(domainObjectLayouts, facetHolder));
        FacetUtil.addFacet(
                CssClassFaFacetForViewModelLayoutAnnotation.create(viewModelLayouts, facetHolder));

        FacetUtil.addFacet(
                DescribedAsFacetForDomainObjectLayoutAnnotation.create(domainObjectLayouts, facetHolder));
        FacetUtil.addFacet(
                DescribedAsFacetForViewModelLayoutAnnotation.create(viewModelLayouts, facetHolder));

        FacetUtil.addFacet(
                NamedFacetForDomainObjectLayoutAnnotation.create(domainObjectLayouts, facetHolder));
        FacetUtil.addFacet(
                NamedFacetForViewModelLayoutAnnotation.create(viewModelLayouts, facetHolder));

        FacetUtil.addFacet(
                PagedFacetForDomainObjectLayoutAnnotation.create(domainObjectLayouts, facetHolder));
        FacetUtil.addFacet(
                PagedFacetForViewModelLayoutAnnotation.create(viewModelLayouts, facetHolder));

        FacetUtil.addFacet(
                PluralFacetForDomainObjectLayoutAnnotation.create(domainObjectLayouts, facetHolder));
        FacetUtil.addFacet(
                PluralFacetForViewModelLayoutAnnotation.create(viewModelLayouts, facetHolder));

        FacetUtil.addFacet(
                BookmarkPolicyFacetForDomainObjectLayoutAnnotation.create(domainObjectLayouts, facetHolder));
        FacetUtil.addFacet(
                BookmarkPolicyFacetForViewModelLayoutAnnotation.create(viewModelLayouts, facetHolder));

        return;
    }




}
