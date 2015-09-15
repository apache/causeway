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

package org.apache.isis.core.metamodel.facets.actions.exploration;

import java.lang.reflect.Method;

import org.jmock.Expectations;

import org.apache.isis.applib.annotation.Exploration;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryProvider;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.actions.exploration.annotation.ExplorationFacetAnnotationFactory;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

public class ExplorationFacetAnnotationFactoryTest extends AbstractFacetFactoryTest {

    private JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    private ExplorationFacetAnnotationFactory facetFactory;

    private TranslationService mockTranslationService;

    private DeploymentCategoryProvider mockDeploymentCategoryProvider;
    private AuthenticationSessionProvider mockAuthenticationSessionProvider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mockDeploymentCategoryProvider = context.mock(DeploymentCategoryProvider.class);
        mockAuthenticationSessionProvider = context.mock(AuthenticationSessionProvider.class);

        final AuthenticationSession mockAuthenticationSession = context.mock(AuthenticationSession.class);
        context.checking(new Expectations() {{
            allowing(mockDeploymentCategoryProvider).getDeploymentCategory();
            will(returnValue(DeploymentCategory.PRODUCTION));

            allowing(mockAuthenticationSessionProvider).getAuthenticationSession();

            will(returnValue(mockAuthenticationSession));
        }});


        facetFactory = new ExplorationFacetAnnotationFactory();
        facetFactory.setDeploymentCategory(DeploymentCategory.PRODUCTION);
        facetFactory.setAuthenticationSessionProvider(mockAuthenticationSessionProvider);
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testExplorationAnnotationPickedUp() {
        class Customer {
            @SuppressWarnings("unused")
            @Exploration
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ExplorationFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ExplorationFacetAbstract);

        assertNoMethodsRemoved();
    }

}
