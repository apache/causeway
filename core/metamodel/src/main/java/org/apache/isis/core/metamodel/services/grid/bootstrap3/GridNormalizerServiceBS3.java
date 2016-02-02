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
package org.apache.isis.core.metamodel.services.grid.bootstrap3;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.bootstrap3.BS3Grid;
import org.apache.isis.applib.layout.common.Grid;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.metamodel.services.grid.GridNormalizerService;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class GridNormalizerServiceBS3 implements GridNormalizerService {

    public static final String TNS = "http://isis.apache.org/schema/applib/layout/bootstrap3";
    public static final String SCHEMA_LOCATION = "http://isis.apache.org/schema/applib/layout/bootstrap3/bootstrap3.xsd";



    @Programmatic
    @Override
    public Class<? extends Grid> gridImplementation() {
        return BS3Grid.class;
    }

    @Programmatic
    @Override
    public String tns() {
        return TNS;
    }

    @Programmatic
    @Override
    public String schemaLocation() {
        return SCHEMA_LOCATION;
    }

    @Override
    public void normalize(final Grid grid, final Class<?> domainClass) {
        BS3Grid bs3Grid = (BS3Grid) grid;

        // TODO
    }


    @Inject
    SpecificationLoader specificationLookup;
    @Inject
    TranslationService translationService;

}
