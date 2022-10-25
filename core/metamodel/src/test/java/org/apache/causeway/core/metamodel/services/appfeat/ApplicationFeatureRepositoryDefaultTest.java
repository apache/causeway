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
package org.apache.causeway.core.metamodel.services.appfeat;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.causeway.applib.services.appfeat.ApplicationFeature;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

class ApplicationFeatureRepositoryDefaultTest {

    ObjectSpecification mockSpec;
    OneToOneAssociation mockProp;
    OneToManyAssociation mockColl;
    ObjectAction mockAct;
    FactoryService mockFactoryService;
    SpecificationLoader mockSpecificationLoader;
    ObjectAction mockActThatIsHidden;
    ServiceRegistry mockServiceRegistry;

    protected ApplicationFeatureRepositoryDefault applicationFeatureRepository;

    @BeforeEach
    public void setUp() throws Exception {

        mockSpecificationLoader = Mockito.mock(SpecificationLoader.class);

        applicationFeatureRepository = new ApplicationFeatureRepositoryDefault(
                /*configuration*/ null,
                mockSpecificationLoader);

        mockActThatIsHidden = Mockito.mock(ObjectAction.class, "mockActThatIsHidden");

        mockSpec = Mockito.mock(ObjectSpecification.class);
        mockProp = Mockito.mock(OneToOneAssociation.class);
        mockColl = Mockito.mock(OneToManyAssociation.class);
        mockAct = Mockito.mock(ObjectAction.class);
        mockFactoryService = Mockito.mock(FactoryService.class);
    }

    public static class AddClassParent extends ApplicationFeatureRepositoryDefaultTest {

        private static ApplicationFeature newApplicationFeature(final ApplicationFeatureId featId) {
            return new ApplicationFeatureDefault(featId);
        }

        @Override
        @BeforeEach
        public void setUp() throws Exception {
            super.setUp();
            mockServiceRegistry = Mockito.mock(ServiceRegistry.class);
            Mockito.when(mockServiceRegistry.streamRegisteredBeans()).thenReturn(Stream.of());
            Mockito.when(mockSpecificationLoader.snapshotSpecifications()).thenReturn(Can.empty());
        }

        @Test
        public void parentAlreadyEncountered() throws Exception {

            // given
            final ApplicationFeatureId packageId = ApplicationFeatureId.newNamespace("com.mycompany");
            final ApplicationFeature pkg = newApplicationFeature(packageId);
            applicationFeatureRepository.namespaceFeatures.put(packageId, pkg);

            final ApplicationFeatureId classFeatureId = ApplicationFeatureId.newType("com.mycompany.Bar");



            // when
            final ApplicationFeatureId applicationFeatureId =
                    applicationFeatureRepository.addClassParent(classFeatureId);

            // then
            assertThat(applicationFeatureId, is(equalTo(packageId)));
        }

    }

}