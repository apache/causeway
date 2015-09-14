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

package org.apache.isis.core.metamodel.app;

import java.util.TreeSet;

import com.google.common.collect.Lists;

import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.runtimecontext.MessageBrokerService;
import org.apache.isis.core.metamodel.runtimecontext.AdapterManager;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryProviderAbstract;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecorator;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.unittestsupport.jmocking.IsisActions;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class IsisMetaModelTest_init {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private IsisConfiguration mockConfiguration;
    @Mock
    private ProgrammingModel mockProgrammingModelFacets;
    @Mock
    private AuthenticationSessionProvider mockAuthenticationSessionProvider;
    @Mock
    private SpecificationLoader mockSpecificationLoader;
    @Mock
    private AdapterManager mockAdapterManager;
    @Mock
    private MessageBrokerService mockMessageBrokerService;
    @Mock
    private ServicesInjector mockDependencyInjector;
    @Mock
    private FacetDecorator mockFacetDecorator;
    @Mock
    private RuntimeContext mockRuntimeContext;

    private IsisMetaModel metaModel;

    @Before
    public void setUp() {
        expectingMetaModelToBeInitialized();
        metaModel = new IsisMetaModel(mockRuntimeContext, mockProgrammingModelFacets);
    }

    private void expectingMetaModelToBeInitialized() {
        final Sequence initSequence = context.sequence("init");
        context.checking(new Expectations() {
            {
                allowing(mockRuntimeContext).injectInto(with(any(Object.class)));
                will(IsisActions.injectInto());
                
                allowing(mockRuntimeContext).getAuthenticationSessionProvider();
                will(returnValue(mockAuthenticationSessionProvider));

                allowing(mockRuntimeContext).getSpecificationLoader();
                will(returnValue(mockSpecificationLoader));

                allowing(mockRuntimeContext).getAdapterManager();
                will(returnValue(mockAdapterManager));

                allowing(mockRuntimeContext).getMessageBrokerService();
                will(returnValue(mockMessageBrokerService));

                allowing(mockRuntimeContext).getServicesInjector();
                will(returnValue(mockDependencyInjector));

                allowing(mockRuntimeContext).getDeploymentCategoryProvider();
                will(returnValue(new DeploymentCategoryProviderAbstract() {
                    @Override
                    public DeploymentCategory getDeploymentCategory() {
                        return DeploymentCategory.PRODUCTION;
                    }
                }));

                oneOf(mockProgrammingModelFacets).init();
                inSequence(initSequence);
                
                oneOf(mockProgrammingModelFacets).getList();
                inSequence(initSequence);
                will(returnValue(Lists.newArrayList()));
                
                oneOf(mockRuntimeContext).init();
                inSequence(initSequence);
            }
        });
        context.ignoring(mockProgrammingModelFacets);
    }

    @Ignore // too much effort, not used
    @Test
    public void shouldSucceedWithoutThrowingAnyExceptions() {
        metaModel.init();
    }

    @Ignore // too much effort, not used
    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToChangeConfiguration() {
        metaModel.init();
        metaModel.setConfiguration(mockConfiguration);
    }

    @Ignore // too much effort, not used
    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToChangeProgrammingModelFacets() {
        metaModel.init();
        metaModel.setProgrammingModelFacets(mockProgrammingModelFacets);
    }

    @Ignore // too much effort, not used
    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToChangeFacetDecorators() {
        metaModel.init();
        metaModel.setFacetDecorators(new TreeSet<FacetDecorator>());
    }

    @Ignore // too much effort, not used
    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotBeAbleToAddToFacetDecorators() {
        metaModel.init();
        metaModel.getFacetDecorators().add(mockFacetDecorator);
    }

    @Ignore // too much effort, not used
    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToInitializeAgain() {
        metaModel.init();
        //
        metaModel.init();
    }

    @Ignore // too much effort, not used
    @Test
    public void shouldPrime() {
        metaModel.init();

    }

}
