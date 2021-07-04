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

import org.apache.isis.core.metamodel.MetaModelTestAbstract;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting.MetaModelContext_forTestingBuilder;

import lombok.val;

class GridLoaderRoundtripTest
extends MetaModelTestAbstract {

    private GridLoaderServiceDefault gridLoaderService;

    @Override
    protected void onSetUp(MetaModelContext_forTestingBuilder mmcBuilder) {
        mmcBuilder.singleton(gridLoaderService = new GridLoaderServiceDefault());
    }

    @Test
    void layoutGeneration() {
        val domainClassAndLayout = new GridLoaderServiceDefault.DomainClassAndLayout(Bar.class, null);
        gridLoaderService.loadXml(domainClassAndLayout);

        getSpecificationLoader().specForType(Bar.class);

        //TODO compare loaded with generated
        //TODO inspect ObjectNamed and MemberNamed (canonical) facets
    }

    // -- HELPER


}