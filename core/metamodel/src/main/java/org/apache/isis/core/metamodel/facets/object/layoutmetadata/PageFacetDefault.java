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
package org.apache.isis.core.metamodel.facets.object.layoutmetadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.layout.members.v1.Page;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.layout.PageService;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.services.layout.provider.PageNormalizerService;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class PageFacetDefault
            extends FacetAbstract
            implements PageFacet {

    private static final Logger LOG = LoggerFactory.getLogger(PageFacetDefault.class);


    public static Class<? extends Facet> type() {
        return PageFacet.class;
    }


    public static PageFacet create(
            final FacetHolder facetHolder,
            final TranslationService translationService,
            final PageService pageService,
            final PageNormalizerService pageNormalizerService, final DeploymentCategory deploymentCategory) {
        return new PageFacetDefault(facetHolder, translationService, pageService, pageNormalizerService,
                deploymentCategory);
    }

    private final TranslationService translationService;
    private final PageNormalizerService pageNormalizerService;
    private final DeploymentCategory deploymentCategory;
    private final PageService pageService;

    private Page page;
    private boolean blacklisted;

    private PageFacetDefault(
            final FacetHolder facetHolder,
            final TranslationService translationService,
            final PageService pageService,
            final PageNormalizerService pageNormalizerService,
            final DeploymentCategory deploymentCategory) {
        super(PageFacetDefault.type(), facetHolder, Derivation.NOT_DERIVED);
        this.pageService = pageService;
        this.translationService = translationService;
        this.pageNormalizerService = pageNormalizerService;
        this.deploymentCategory = deploymentCategory;
    }

    /**
     * Blacklisting only occurs if running in production mode.
     */
    @Override
    public Page getPage() {
        if (deploymentCategory.isProduction() || blacklisted) {
            return page;
        }
        final Class<?> domainClass = getSpecification().getCorrespondingClass();
        final Page page = pageService.fromXml(domainClass);
        if(deploymentCategory.isProduction() && page == null) {
            blacklisted = true;
        }
        this.page = normalize(page);
        return this.page;
    }

    private Page normalize(final Page page) {
        if(page == null) {
            return null;
        }

        // if have .layout.json and then add a .layout.xml without restarting, then note that
        // the changes won't be picked up.  Normalizing would be required
        // in order to trample over the .layout.json's original facets
        if(page.isNormalized()) {
            return page;
        }

        final Class<?> domainClass = getSpecification().getCorrespondingClass();

        pageNormalizerService.normalize(page, domainClass);
        return page;
    }

    private ObjectSpecification getSpecification() {
        return (ObjectSpecification) getFacetHolder();
    }



}
