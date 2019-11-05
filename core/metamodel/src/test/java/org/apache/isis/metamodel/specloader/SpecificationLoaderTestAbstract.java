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

package org.apache.isis.metamodel.specloader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.ConfigurableEnvironment;

import org.apache.isis.applib.services.grid.GridService;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.i18n.TranslationService.Mode;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.MetaModelContext_forTesting;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.facets.object.plural.PluralFacet;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.metamodel.progmodel.ProgrammingModelAbstract;
import org.apache.isis.metamodel.progmodel.ProgrammingModelInitFilterDefault;
import org.apache.isis.metamodel.progmodels.dflt.ProgrammingModelFacetsJava8;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.security.authentication.AuthenticationSessionProvider;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import lombok.val;

abstract class SpecificationLoaderTestAbstract {

    static class Producers {

        ConfigurableEnvironment newConfigurableEnvironment() {
            val mock = Mockito.mock(ConfigurableEnvironment.class);
            when(mock.getProperty("")).thenReturn("nop");
            return mock;
        }

        IsisConfiguration newConfiguration() {
            val config = new IsisConfiguration(); // uses defaults!
            config.setEnvironment(newConfigurableEnvironment());
            return config;
        }

        AuthenticationSessionProvider mockAuthenticationSessionProvider() {
            return Mockito.mock(AuthenticationSessionProvider.class);
        }

        GridService mockGridService() {
            return Mockito.mock(GridService.class);
        }

        MessageService mockMessageService() {
            return Mockito.mock(MessageService.class);
        }

        TitleService mockTitleService() {
            val mock = Mockito.mock(TitleService.class);
            return mock;
        }
        
        TranslationService mockTranslationService() {
            val mock = Mockito.mock(TranslationService.class);
            when(mock.getMode()).thenReturn(Mode.DISABLED);
            return mock;
        }

        ServiceInjector getServiceInjector() {
            return Mockito.mock(ServiceInjector.class);
        }
        
        ProgrammingModel getProgrammingModel() {
            return new ProgrammingModelFacetsJava8();
        }
        
    }

    protected IsisConfiguration isisConfiguration;
    protected SpecificationLoader specificationLoader;
    protected AuthenticationSessionProvider mockAuthenticationSessionProvider;
    protected GridService mockGridService;
    protected MessageService mockMessageService;
    protected MetaModelContext metaModelContext;

    // is loaded by subclasses
    protected ObjectSpecification specification;

    @BeforeEach
    public void setUp() throws Exception {

        // PRODUCTION

        val producers = new Producers();
        
        val programmingModel = producers.getProgrammingModel();

        metaModelContext = MetaModelContext_forTesting.builder()
                .configuration(isisConfiguration = producers.newConfiguration())
                .programmingModel(programmingModel)
                .translationService(producers.mockTranslationService())
                .titleService(producers.mockTitleService())
//                .objectAdapterProvider(mockPersistenceSessionServiceInternal = producers.mockPersistenceSessionServiceInternal())
                .authenticationSessionProvider(mockAuthenticationSessionProvider = producers.mockAuthenticationSessionProvider())
                .singleton(mockMessageService = producers.mockMessageService())
                .singleton(mockGridService = producers.mockGridService())
                .build();
        
        specificationLoader = metaModelContext.getSpecificationLoader();
        
        ((ProgrammingModelAbstract)programmingModel).init(new ProgrammingModelInitFilterDefault(), metaModelContext);

        specificationLoader.createMetaModel();

        specification = loadSpecification(specificationLoader);

    }

    @AfterEach
    public void tearDown() throws Exception {
        specificationLoader.disposeMetaModel();
    }

    protected abstract ObjectSpecification loadSpecification(SpecificationLoader reflector);

    @Test
    public void testCollectionFacet() throws Exception {
        final Facet facet = specification.getFacet(CollectionFacet.class);
        assertNull(facet);
    }


    @Test
    public void testTypeOfFacet() throws Exception {
        final TypeOfFacet facet = specification.getFacet(TypeOfFacet.class);
        assertNull(facet);
    }


    @Test
    public void testNamedFaced() throws Exception {
        final Facet facet = specification.getFacet(NamedFacet.class);
        assertNotNull(facet);
    }

    @Test
    public void testPluralFaced() throws Exception {
        final Facet facet = specification.getFacet(PluralFacet.class);
        assertNotNull(facet);
    }

    @Test
    public void testDescriptionFacet() throws Exception {
        final Facet facet = specification.getFacet(DescribedAsFacet.class);
        assertNotNull(facet);
    }

}
