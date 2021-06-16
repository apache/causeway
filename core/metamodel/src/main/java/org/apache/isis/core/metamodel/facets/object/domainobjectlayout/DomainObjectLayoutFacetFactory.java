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

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.services.events.MetamodelEventService;

import lombok.val;

public class DomainObjectLayoutFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public DomainObjectLayoutFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {

        val facetHolder = processClassContext.getFacetHolder();
        val domainObjectLayoutIfAny = processClassContext.synthesizeOnType(DomainObjectLayout.class);
        val metamodelEventService = this.metamodelEventService.get();

        addFacetIfPresent(
                TitleFacetViaDomainObjectLayoutAnnotationUsingTitleUiEvent
                .create(domainObjectLayoutIfAny, metamodelEventService, getConfiguration(), facetHolder));

        addFacetIfPresent(
                IconFacetViaDomainObjectLayoutAnnotationUsingIconUiEvent
                .create(domainObjectLayoutIfAny, metamodelEventService, getConfiguration(), facetHolder));

        addFacetIfPresent(
                CssClassFacetViaDomainObjectLayoutAnnotationUsingCssClassUiEvent
                .create(domainObjectLayoutIfAny, metamodelEventService, getConfiguration(), facetHolder));

        addFacetIfPresent(
                LayoutFacetViaDomainObjectLayoutAnnotationUsingLayoutUiEvent
                .create(domainObjectLayoutIfAny, metamodelEventService, getConfiguration(), facetHolder));

        addFacetIfPresent(
                CssClassFacetForDomainObjectLayoutAnnotation
                .create(domainObjectLayoutIfAny, facetHolder));

        addFacetIfPresent(
                CssClassFaFacetForDomainObjectLayoutAnnotation
                .create(domainObjectLayoutIfAny, facetHolder));

        addFacetIfPresent(
                DescribedAsFacetForDomainObjectLayoutAnnotation
                .create(domainObjectLayoutIfAny, facetHolder));

        addFacetIfPresent(
                NamedFacetForDomainObjectLayoutAnnotation
                .create(domainObjectLayoutIfAny, facetHolder));

        addFacetIfPresent(
                PagedFacetForDomainObjectLayoutAnnotation
                .create(domainObjectLayoutIfAny, facetHolder));

        addFacetIfPresent(
                PluralFacetForDomainObjectLayoutAnnotation
                .create(domainObjectLayoutIfAny, facetHolder));

        addFacetIfPresent(
                BookmarkPolicyFacetForDomainObjectLayoutAnnotation
                .create(domainObjectLayoutIfAny, facetHolder));

        return;
    }

    private final _Lazy<MetamodelEventService> metamodelEventService = _Lazy.threadSafe(()->
        getServiceRegistry().lookupServiceElseFail(MetamodelEventService.class));


}
