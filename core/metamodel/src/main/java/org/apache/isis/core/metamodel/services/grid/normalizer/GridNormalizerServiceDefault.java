/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.services.grid.normalizer;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.bootstrap3.BS3Grid;
import org.apache.isis.applib.layout.fixedcols.FCGrid;
import org.apache.isis.applib.layout.common.Grid;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class GridNormalizerServiceDefault implements GridNormalizerService, SpecificationLoaderAware {

    private static final Logger LOG = LoggerFactory.getLogger(GridNormalizerServiceDefault.class);

    public static final String COMMON_TNS = "http://isis.apache.org/schema/applib/layout/common";
    public static final String COMMON_SCHEMA_LOCATION = "http://isis.apache.org/schema/applib/layout/common/common.xsd";

    private SpecificationLoader specificationLookup;

    private GridNormalizerFC pageNormalizerFC;
    private GridNormalizerBS3 pageNormalizerBS3;

    @PostConstruct
    @Programmatic
    public void init() {
        pageNormalizerFC = new GridNormalizerFC(translationService, specificationLookup);
        pageNormalizerBS3 = new GridNormalizerBS3(translationService, specificationLookup);
    }

    @Programmatic
    @Override
    public List<Class<? extends Grid>> pageImplementations() {
        List<Class<? extends Grid>> ar = Lists.newArrayList();
        ar.add(FCGrid.class);
        ar.add(BS3Grid.class);
        return ar;
    }

    @Override
    public void normalize(final Grid grid, final Class<?> domainClass) {

        if(grid instanceof FCGrid) {
            final FCGrid fcPage = (FCGrid) grid;
            pageNormalizerFC.normalize(fcPage, domainClass);
        } else
        if(grid instanceof BS3Grid) {
            final BS3Grid bs3Page = (BS3Grid) grid;
            pageNormalizerBS3.normalize(bs3Page, domainClass);
        }

        grid.setNormalized(true);
    }

    @Override
    public String schemaLocationsFor(final Grid grid) {
        final List<String> parts = Lists.newArrayList();
        parts.add(COMMON_TNS);
        parts.add(COMMON_SCHEMA_LOCATION);
        if(grid instanceof FCGrid) {
            parts.add(GridNormalizerFC.TNS);
            parts.add(GridNormalizerFC.SCHEMA_LOCATION);
        }
        if(grid instanceof BS3Grid) {
            parts.add(GridNormalizerBS3.TNS);
            parts.add(GridNormalizerBS3.SCHEMA_LOCATION);
        }
        return Joiner.on(" ").join(parts);
    }

    @Override
    public void setSpecificationLoader(final SpecificationLoader specificationLookup) {
        this.specificationLookup = specificationLookup;
    }

    @Inject
    TranslationService translationService;

}
