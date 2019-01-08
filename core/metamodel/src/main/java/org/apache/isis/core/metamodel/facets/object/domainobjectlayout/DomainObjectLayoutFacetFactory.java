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
package org.apache.isis.core.metamodel.facets.object.domainobjectlayout;

import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.ViewModelLayout;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;

public class DomainObjectLayoutFacetFactory extends FacetFactoryAbstract {


    public DomainObjectLayoutFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();

        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final DomainObjectLayout domainObjectLayout = Annotations.getAnnotation(cls, DomainObjectLayout.class);
        final ViewModelLayout viewModelLayout = Annotations.getAnnotation(cls, ViewModelLayout.class);

        FacetUtil.addFacet(
                TitleFacetViaDomainObjectLayoutAnnotationUsingTitleUiEvent.create(
                        domainObjectLayout, servicesInjector, getConfiguration(), facetHolder));
        FacetUtil.addFacet(
                IconFacetViaDomainObjectLayoutAnnotationUsingIconUiEvent.create(
                        domainObjectLayout, servicesInjector, getConfiguration(), facetHolder));
        FacetUtil.addFacet(
                CssClassFacetViaDomainObjectLayoutAnnotationUsingCssClassUiEvent.create(
                        domainObjectLayout, servicesInjector, getConfiguration(), facetHolder));
        FacetUtil.addFacet(
                LayoutFacetViaDomainObjectLayoutAnnotationUsingLayoutUiEvent.create(
                        domainObjectLayout, servicesInjector, getConfiguration(), facetHolder));

        FacetUtil.addFacet(
                CssClassFacetForDomainObjectLayoutAnnotation.create(domainObjectLayout, facetHolder));
        FacetUtil.addFacet(
                CssClassFacetForViewModelLayoutAnnotation.create(viewModelLayout, facetHolder));

        FacetUtil.addFacet(
                CssClassFaFacetForDomainObjectLayoutAnnotation.create(domainObjectLayout, facetHolder));
        FacetUtil.addFacet(
                CssClassFaFacetForViewModelLayoutAnnotation.create(viewModelLayout, facetHolder));

        FacetUtil.addFacet(
                DescribedAsFacetForDomainObjectLayoutAnnotation.create(domainObjectLayout, facetHolder));
        FacetUtil.addFacet(
                DescribedAsFacetForViewModelLayoutAnnotation.create(viewModelLayout, facetHolder));

        FacetUtil.addFacet(
                NamedFacetForDomainObjectLayoutAnnotation.create(domainObjectLayout, facetHolder));
        FacetUtil.addFacet(
                NamedFacetForViewModelLayoutAnnotation.create(viewModelLayout, facetHolder));

        FacetUtil.addFacet(
                PagedFacetForDomainObjectLayoutAnnotation.create(domainObjectLayout, facetHolder));
        FacetUtil.addFacet(
                PagedFacetForViewModelLayoutAnnotation.create(viewModelLayout, facetHolder));

        FacetUtil.addFacet(
                PluralFacetForDomainObjectLayoutAnnotation.create(domainObjectLayout, facetHolder));
        FacetUtil.addFacet(
                PluralFacetForViewModelLayoutAnnotation.create(viewModelLayout, facetHolder));

        FacetUtil.addFacet(
                BookmarkPolicyFacetForDomainObjectLayoutAnnotation.create(domainObjectLayout, facetHolder));
        FacetUtil.addFacet(
                BookmarkPolicyFacetForViewModelLayoutAnnotation.create(viewModelLayout, facetHolder));

        return;
    }




}
