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

package org.apache.isis.core.metamodel.specloader;

import com.google.common.collect.Lists;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.apache.isis.applib.services.grid.GridService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryProvider;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.plural.PluralFacet;
import org.apache.isis.core.metamodel.layoutmetadata.LayoutMetadataReader;
import org.apache.isis.core.metamodel.layoutmetadata.json.LayoutMetadataReaderFromJson;
import org.apache.isis.core.metamodel.metamodelvalidator.dflt.MetaModelValidatorDefault;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.progmodels.dflt.ProgrammingModelFacetsJava5;

public abstract class SpecificationLoaderTestAbstract {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private DeploymentCategoryProvider mockDeploymentCategoryProvider;
    @Mock
    private AuthenticationSessionProvider mockAuthenticationSessionProvider;
    @Mock
    private GridService mockGridService;
    @Mock
    private PersistenceSessionServiceInternal mockPersistenceSessionServiceInternal;
    @Mock
    private MessageService mockMessageService;

    ServicesInjector stubServicesInjector;
    IsisConfigurationDefault stubConfiguration;

    // is loaded by subclasses
    protected ObjectSpecification specification;


    @Before
    public void setUp() throws Exception {

        context.checking(new Expectations() {{

            allowing(mockDeploymentCategoryProvider).getDeploymentCategory();
            will(returnValue(DeploymentCategory.PRODUCTION));

            ignoring(mockGridService).existsFor(with(any(Class.class)));

            ignoring(mockPersistenceSessionServiceInternal);
            ignoring(mockMessageService);

        }});

        stubConfiguration = new IsisConfigurationDefault(null);

        stubServicesInjector =
                new ServicesInjector(
                    Lists.newArrayList(
                        mockAuthenticationSessionProvider,
                        stubConfiguration,
                        mockDeploymentCategoryProvider,
                        mockPersistenceSessionServiceInternal,
                            mockMessageService,
                        mockGridService,
                        mockDeploymentCategoryProvider),
                    stubConfiguration);

        final SpecificationLoader specificationLoader =
                new SpecificationLoader(
                        stubConfiguration, new ProgrammingModelFacetsJava5(stubConfiguration),
                        new MetaModelValidatorDefault(), Lists.<LayoutMetadataReader>newArrayList(
                                new LayoutMetadataReaderFromJson()), stubServicesInjector);

        stubServicesInjector.addFallbackIfRequired(SpecificationLoader.class, specificationLoader);

        specificationLoader.init();
        
        specification = loadSpecification(specificationLoader);
    }

    protected abstract ObjectSpecification loadSpecification(SpecificationLoader reflector);

    @Ignore("broken")
    @Test
    public void testCollectionFacet() throws Exception {
        final Facet facet = specification.getFacet(CollectionFacet.class);
        Assert.assertNull(facet);
    }

    @Ignore("broken")
    @Test
    public void testTypeOfFacet() throws Exception {
        final TypeOfFacet facet = specification.getFacet(TypeOfFacet.class);
        Assert.assertNull(facet);
    }

    @Ignore("broken")
    @Test
    public void testNamedFaced() throws Exception {
        final Facet facet = specification.getFacet(NamedFacet.class);
        Assert.assertNotNull(facet);
    }

    @Ignore("broken")
    @Test
    public void testPluralFaced() throws Exception {
        final Facet facet = specification.getFacet(PluralFacet.class);
        Assert.assertNotNull(facet);
    }

    @Ignore("broken")
    @Test
    public void testDescriptionFacet() throws Exception {
        final Facet facet = specification.getFacet(DescribedAsFacet.class);
        Assert.assertNotNull(facet);
    }

}
