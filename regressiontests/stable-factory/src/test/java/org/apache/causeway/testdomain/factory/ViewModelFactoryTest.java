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
package org.apache.causeway.testdomain.factory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
        }
)
@TestPropertySource(CausewayPresets.UseLog4j2Test)
class ViewModelFactoryTest extends CausewayIntegrationTestAbstract {

    // -- VIEW MODEL SAMPLES

    @DomainObject(nature = Nature.VIEW_MODEL)
    public static class SimpleViewModel {
        @Inject private RepositoryService repository;
        private boolean postConstructCalled;

        public void assertInitialized() {
            assertNotNull(repository, ()->"repository (field) not injected");
            assertTrue(postConstructCalled, ()->"@PostConstruct not called");
        }

        @PostConstruct
        void onPostConstruct() {
            postConstructCalled = true;
        }

    }

    @DomainObject(nature = Nature.VIEW_MODEL)
    @RequiredArgsConstructor
    public static class ViewModelWithInjectableFields implements ViewModel {

        @Inject private RepositoryService repository;
        @Getter private final String name;
        private boolean postConstructCalled;

        public void assertInitialized() {
            assertNotNull(repository, ()->"repository (field) not injected");
            assertEquals("aName", getName(), ()->"unexpected name (constructor arg)");
            assertTrue(postConstructCalled, ()->"@PostConstruct not called");
        }

        @Override
        public String viewModelMemento() {
            return "aName";
        }

        @PostConstruct
        void onPostConstruct() {
            postConstructCalled = true;
        }
    }


    @DomainObject(nature = Nature.VIEW_MODEL)
    @RequiredArgsConstructor
    public static class ViewModelWithInjectableConstructorArgs implements ViewModel {

        @Inject private RepositoryService repository;
        private final ServiceRegistry registry;
        @Getter private final String name;
        private boolean postConstructCalled;

        public void assertInitialized() {
            assertNotNull(repository, ()->"repository (field) not injected");
            assertNotNull(registry, ()->"registry (constructor arg) not injected");
            assertEquals("aName", getName(), ()->"unexpected name (constructor arg)");
            assertTrue(postConstructCalled, ()->"@PostConstruct not called");
        }

        @Override
        public String viewModelMemento() {
            return "aName";
        }

        @PostConstruct
        void onPostConstruct() {
            postConstructCalled = true;
        }

    }

    // -- TESTS

    @Test
    void sampleViewModel_shouldHave_injectionPointsResolved() {
        val sampleViewModel = factoryService.viewModel(SimpleViewModel.class);
        sampleViewModel.assertInitialized();
    }

    @Test
    void viewModel_shouldHave_injectionPointsResolved() {
        val viewModel = factoryService.viewModel(new ViewModelWithInjectableFields("aName"));
        viewModel.assertInitialized();
    }

    @Test
    void viewModel_shouldHave_constructorArgsResolved() {
        ViewModelWithInjectableConstructorArgs viewModel = factoryService.viewModel(ViewModelWithInjectableConstructorArgs.class,
                Bookmark.forLogicalTypeNameAndIdentifier(
                        ViewModelWithInjectableConstructorArgs.class.getName(),
                        "aName"));
        viewModel.assertInitialized();
    }

}
