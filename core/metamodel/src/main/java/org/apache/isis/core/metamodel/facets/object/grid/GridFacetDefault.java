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
package org.apache.isis.core.metamodel.facets.object.grid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.layout.common.Grid;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.layout.GridService;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.services.grid.normalizer.GridNormalizerService;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class GridFacetDefault
            extends FacetAbstract
            implements GridFacet {

    private static final Logger LOG = LoggerFactory.getLogger(GridFacetDefault.class);


    public static Class<? extends Facet> type() {
        return GridFacet.class;
    }


    public static GridFacet create(
            final FacetHolder facetHolder,
            final TranslationService translationService,
            final GridService gridService,
            final GridNormalizerService gridNormalizerService, final DeploymentCategory deploymentCategory) {
        return new GridFacetDefault(facetHolder, translationService, gridService, gridNormalizerService,
                deploymentCategory);
    }

    private final TranslationService translationService;
    private final GridNormalizerService gridNormalizerService;
    private final DeploymentCategory deploymentCategory;
    private final GridService gridService;

    private Grid grid;
    private boolean blacklisted;

    private GridFacetDefault(
            final FacetHolder facetHolder,
            final TranslationService translationService,
            final GridService gridService,
            final GridNormalizerService gridNormalizerService,
            final DeploymentCategory deploymentCategory) {
        super(GridFacetDefault.type(), facetHolder, Derivation.NOT_DERIVED);
        this.gridService = gridService;
        this.translationService = translationService;
        this.gridNormalizerService = gridNormalizerService;
        this.deploymentCategory = deploymentCategory;
    }

    /**
     * Blacklisting only occurs if running in production mode.
     */
    public Grid getGrid() {
        if (deploymentCategory.isProduction() || blacklisted) {
            return this.grid;
        }
        final Class<?> domainClass = getSpecification().getCorrespondingClass();
        final Grid grid = gridService.fromXml(domainClass);
        if(deploymentCategory.isProduction() && grid == null) {
            blacklisted = true;
        }
        this.grid = normalize(grid);
        return this.grid;
    }

    private Grid normalize(final Grid grid) {
        if(grid == null) {
            return null;
        }

        // if have .layout.json and then add a .layout.xml without restarting, then note that
        // the changes won't be picked up.  Normalizing would be required
        // in order to trample over the .layout.json's original facets
        if(grid.isNormalized()) {
            return grid;
        }

        final Class<?> domainClass = getSpecification().getCorrespondingClass();

        gridNormalizerService.normalize(grid, domainClass);
        return grid;
    }

    private ObjectSpecification getSpecification() {
        return (ObjectSpecification) getFacetHolder();
    }



}
