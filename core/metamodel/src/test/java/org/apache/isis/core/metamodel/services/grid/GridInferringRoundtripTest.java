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
package org.apache.isis.core.metamodel.services.grid;

import org.junit.jupiter.api.Test;

import org.apache.isis.applib.services.grid.GridLoaderService;
import org.apache.isis.applib.services.layout.LayoutService;
import org.apache.isis.applib.services.layout.Style;
import org.apache.isis.core.metamodel.MetaModelTestAbstract;

import lombok.val;

class GridInferringRoundtripTest
extends MetaModelTestAbstract {

    private GridLoaderServiceDefault gridLoaderService;
    private LayoutService layoutService;

    @Override
    protected void afterSetUp() {
        layoutService = getServiceRegistry().lookupServiceElseFail(LayoutService.class);
        gridLoaderService = (GridLoaderServiceDefault)getServiceRegistry().lookupServiceElseFail(GridLoaderService.class);
    }

    @Test
    void layoutGeneration() {
        val domainClassAndLayout = new GridLoaderServiceDefault.DomainClassAndLayout(Bar.class, null);
        gridLoaderService.loadXml(domainClassAndLayout);

        getSpecificationLoader().specForType(Bar.class);

        val xml = layoutService.toXml(Bar.class, Style.NORMALIZED);

        System.out.println(xml);


        //TODO compare loaded with generated
        //TODO inspect ObjectNamed and MemberNamed (canonical) facets
    }

    // -- HELPER


}