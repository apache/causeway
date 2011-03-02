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

package org.apache.isis.defaults.progmodel;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecorator;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.object.plural.PluralFacet;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.ObjectReflectorDefault;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistryDefault;
import org.apache.isis.core.metamodel.specloader.traverser.SpecificationTraverserDefault;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorNoop;
import org.apache.isis.core.progmodel.layout.dflt.MemberLayoutArrangerDefault;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.context.IsisContextStatic;
import org.apache.isis.core.runtime.imageloader.TemplateImageLoader;
import org.apache.isis.core.runtime.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.core.runtime.session.IsisSessionFactory;
import org.apache.isis.core.runtime.session.IsisSessionFactoryDefault;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.testsystem.TestClassSubstitutor;
import org.apache.isis.core.runtime.userprofile.UserProfileLoader;

@RunWith(JMock.class)
public abstract class JavaReflectorTestAbstract {

    private final Mockery mockery = new JUnit4Mockery();

    protected ObjectSpecification specification;
    protected TemplateImageLoader mockTemplateImageLoader;
    protected PersistenceSessionFactory mockPersistenceSessionFactory;
    private UserProfileLoader mockUserProfileLoader;
    protected AuthenticationManager mockAuthenticationManager;
    protected AuthorizationManager mockAuthorizationManager;

    private List<Object> servicesList;

    @Before
    public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        IsisConfigurationDefault configuration = new IsisConfigurationDefault();

        mockTemplateImageLoader = mockery.mock(TemplateImageLoader.class);
        mockPersistenceSessionFactory = mockery.mock(PersistenceSessionFactory.class);
        mockUserProfileLoader = mockery.mock(UserProfileLoader.class);
        mockAuthenticationManager = mockery.mock(AuthenticationManager.class);
        mockAuthorizationManager = mockery.mock(AuthorizationManager.class);
        servicesList = Collections.emptyList();

        mockery.checking(new Expectations() {
            {
                ignoring(mockTemplateImageLoader);
                ignoring(mockPersistenceSessionFactory);
                ignoring(mockUserProfileLoader);
                ignoring(mockAuthenticationManager);
                ignoring(mockAuthorizationManager);
            }
        });

        final ObjectReflectorDefault reflector =
            new ObjectReflectorDefault(configuration, new TestClassSubstitutor(), new CollectionTypeRegistryDefault(),
                new SpecificationTraverserDefault(), new MemberLayoutArrangerDefault(), new ProgrammingModelFacetsJava5(), new HashSet<FacetDecorator>(),
                new MetaModelValidatorNoop());
        reflector.setRuntimeContext(new RuntimeContextFromSession());
        reflector.init();

        // not sure if this is needed since we have now moved Reflector out to global scope,
        // not specific to an ExecutionContext.
        IsisSessionFactory executionContextFactory =
            new IsisSessionFactoryDefault(DeploymentType.EXPLORATION, configuration, mockTemplateImageLoader,
                reflector, mockAuthenticationManager, mockAuthorizationManager, mockUserProfileLoader,
                mockPersistenceSessionFactory, servicesList);
        IsisContextStatic.createRelaxedInstance(executionContextFactory);
        IsisContextStatic.getInstance().getSessionInstance(); // cause an Execution Context to load

        specification = loadSpecification(reflector);
    }

    protected abstract ObjectSpecification loadSpecification(ObjectReflectorDefault reflector);

    @Test
    public void testCollectionFacet() throws Exception {
        final Facet facet = specification.getFacet(CollectionFacet.class);
        Assert.assertNull(facet);
    }

    @Test
    public void testTypeOfFacet() throws Exception {
        final TypeOfFacet facet = specification.getFacet(TypeOfFacet.class);
        Assert.assertNull(facet);
    }

    @Test
    public void testNamedFaced() throws Exception {
        final Facet facet = specification.getFacet(NamedFacet.class);
        Assert.assertNotNull(facet);
    }

    @Test
    public void testPluralFaced() throws Exception {
        final Facet facet = specification.getFacet(PluralFacet.class);
        Assert.assertNotNull(facet);
    }

    @Test
    public void testDescriptionFacet() throws Exception {
        final Facet facet = specification.getFacet(DescribedAsFacet.class);
        Assert.assertNotNull(facet);
    }

}
