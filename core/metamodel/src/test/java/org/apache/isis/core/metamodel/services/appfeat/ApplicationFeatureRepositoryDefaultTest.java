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
package org.apache.isis.core.metamodel.services.appfeat;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.services.appfeat.ApplicationFeature;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

//FIXME[ISIS-3207]
@DisabledIfSystemProperty(named = "isRunningWithSurefire", matches = "true")
@ExtendWith(MockitoExtension.class)
class ApplicationFeatureRepositoryDefaultTest {

    @Mock ObjectSpecification mockSpec;
    @Mock OneToOneAssociation mockProp;
    @Mock OneToManyAssociation mockColl;
    @Mock ObjectAction mockAct;

    ObjectAction mockActThatIsHidden;

    @Mock FactoryService mockFactoryService;
    @Mock ServiceRegistry mockServiceRegistry;
    @Mock SpecificationLoader mockSpecificationLoader;

    protected ApplicationFeatureRepositoryDefault applicationFeatureRepository;

    @BeforeEach
    public void setUp() throws Exception {

        applicationFeatureRepository = new ApplicationFeatureRepositoryDefault(
                /*configuration*/ null,
                mockSpecificationLoader);

        mockActThatIsHidden = Mockito.mock(ObjectAction.class, "mockActThatIsHidden");
    }

  //FIXME[ISIS-3207]
    @DisabledIfSystemProperty(named = "isRunningWithSurefire", matches = "true")
    public static class AddClassParent extends ApplicationFeatureRepositoryDefaultTest {

        private static ApplicationFeature newApplicationFeature(final ApplicationFeatureId featId) {
            return new ApplicationFeatureDefault(featId);
        }

        @Override
        @BeforeEach
        public void setUp() throws Exception {
            super.setUp();
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