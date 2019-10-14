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

import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.ViewModelLayout;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.services.events.MetamodelEventService;

import lombok.val;

public class DomainObjectLayoutFacetFactory extends FacetFactoryAbstract {

    public DomainObjectLayoutFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        val facetHolder = processClassContext.getFacetHolder();

        val domainObjectLayoutIfAny = processClassContext.synthesizeOnType(DomainObjectLayout.class);
        val viewModelLayoutIfAny = processClassContext.synthesizeOnType(ViewModelLayout.class);

        val metamodelEventService = this.metamodelEventService.get();

        FacetUtil.addFacet(
                TitleFacetViaDomainObjectLayoutAnnotationUsingTitleUiEvent.create(
                        domainObjectLayoutIfAny, metamodelEventService, getConfiguration(), facetHolder));
        FacetUtil.addFacet(
                TitleFacetViaViewModelLayoutAnnotationUsingTitleUiEvent.create(
                        viewModelLayoutIfAny, metamodelEventService, getConfiguration(), facetHolder));

        FacetUtil.addFacet(
                IconFacetViaDomainObjectLayoutAnnotationUsingIconUiEvent.create(
                        domainObjectLayoutIfAny, metamodelEventService, getConfiguration(), facetHolder));
        FacetUtil.addFacet(
                IconFacetViaViewModelLayoutAnnotationUsingIconUiEvent.create(
                        viewModelLayoutIfAny, metamodelEventService, getConfiguration(), facetHolder));

        FacetUtil.addFacet(
                CssClassFacetViaDomainObjectLayoutAnnotationUsingCssClassUiEvent.create(
                        domainObjectLayoutIfAny, metamodelEventService, getConfiguration(), facetHolder));
        FacetUtil.addFacet(
                CssClassFacetViaViewModelLayoutAnnotationUsingCssClassUiEvent.create(
                        viewModelLayoutIfAny, metamodelEventService, getConfiguration(), facetHolder));

        FacetUtil.addFacet(
                LayoutFacetViaDomainObjectLayoutAnnotationUsingLayoutUiEvent.create(
                        domainObjectLayoutIfAny, metamodelEventService, getConfiguration(), facetHolder));
        FacetUtil.addFacet(
                LayoutFacetViaViewModelLayoutAnnotationUsingLayoutUiEvent.create(
                        viewModelLayoutIfAny, metamodelEventService, getConfiguration() , facetHolder));

        FacetUtil.addFacet(
                CssClassFacetForDomainObjectLayoutAnnotation.create(domainObjectLayoutIfAny, facetHolder));
        FacetUtil.addFacet(
                CssClassFacetForViewModelLayoutAnnotation.create(viewModelLayoutIfAny, facetHolder));

        FacetUtil.addFacet(
                CssClassFaFacetForDomainObjectLayoutAnnotation.create(domainObjectLayoutIfAny, facetHolder));
        FacetUtil.addFacet(
                CssClassFaFacetForViewModelLayoutAnnotation.create(viewModelLayoutIfAny, facetHolder));

        FacetUtil.addFacet(
                DescribedAsFacetForDomainObjectLayoutAnnotation.create(domainObjectLayoutIfAny, facetHolder));
        FacetUtil.addFacet(
                DescribedAsFacetForViewModelLayoutAnnotation.create(viewModelLayoutIfAny, facetHolder));

        FacetUtil.addFacet(
                NamedFacetForDomainObjectLayoutAnnotation.create(domainObjectLayoutIfAny, facetHolder));
        FacetUtil.addFacet(
                NamedFacetForViewModelLayoutAnnotation.create(viewModelLayoutIfAny, facetHolder));

        FacetUtil.addFacet(
                PagedFacetForDomainObjectLayoutAnnotation.create(domainObjectLayoutIfAny, facetHolder));
        FacetUtil.addFacet(
                PagedFacetForViewModelLayoutAnnotation.create(viewModelLayoutIfAny, facetHolder));

        FacetUtil.addFacet(
                PluralFacetForDomainObjectLayoutAnnotation.create(domainObjectLayoutIfAny, facetHolder));
        FacetUtil.addFacet(
                PluralFacetForViewModelLayoutAnnotation.create(viewModelLayoutIfAny, facetHolder));

        FacetUtil.addFacet(
                BookmarkPolicyFacetForDomainObjectLayoutAnnotation.create(domainObjectLayoutIfAny, facetHolder));
        FacetUtil.addFacet(
                BookmarkPolicyFacetForViewModelLayoutAnnotation.create(viewModelLayoutIfAny, facetHolder));

        return;
    }

    private final _Lazy<MetamodelEventService> metamodelEventService = _Lazy.threadSafe(()->
        getServiceRegistry().lookupServiceElseFail(MetamodelEventService.class));


}
