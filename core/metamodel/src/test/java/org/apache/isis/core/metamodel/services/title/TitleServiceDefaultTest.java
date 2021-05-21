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
package org.apache.isis.core.metamodel.services.title;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelAbstract;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelInitFilterDefault;
import org.apache.isis.core.metamodel.progmodels.dflt.ProgrammingModelFacetsJava8;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import lombok.val;

class TitleServiceDefaultTest {

    private ProgrammingModelFacetsJava8 programmingModel;
    private MetaModelContext metaModelContext;
    private TitleServiceDefault titleService;
    
    @BeforeEach
    void setUp() throws Exception {
        
        val mockServiceInjector = Mockito.mock(ServiceInjector.class);
        when(mockServiceInjector.injectServicesInto(ArgumentMatchers.any())).thenAnswer(i -> i.getArguments()[0]);
        programmingModel = new ProgrammingModelFacetsJava8(mockServiceInjector);
        
        metaModelContext = MetaModelContext_forTesting.builder()
                .programmingModel(programmingModel)
                .titleService(new TitleServiceDefault(null, null)) // not used by this test, but required to init
                .serviceInjector(mockServiceInjector)
                .build();
        
        ((ProgrammingModelAbstract)programmingModel)
        .init(new ProgrammingModelInitFilterDefault(), metaModelContext);
        
        metaModelContext.getSpecificationLoader().createMetaModel();
        
        titleService = new TitleServiceDefault(null, metaModelContext.getObjectManager());

    }

    @AfterEach
    void tearDown() throws Exception {
    }
    
    // -- FEATURED

    public static enum FeaturedEnum {
        FIRST,
        SECOND;
        
        public String title() {
            return name().toLowerCase();
        }
        
        public String iconName() {
            return name().toLowerCase();
        }
        
    }
    
    @Test
    void enum_shouldHonorTitleByMethod() {

        Object domainObject = FeaturedEnum.FIRST;

        val title = titleService.titleOf(domainObject);
        assertEquals("first", title);

    }
    
    // -- PLAIN
    
    public static enum PlainEnum {
        FIRST,
        SECOND
    }

    @Test
    void enum_shouldFallbackTitleToEnumName() {

        Object domainObject = PlainEnum.FIRST;

        val title = titleService.titleOf(domainObject);
        assertEquals("First", title);

    }

}
