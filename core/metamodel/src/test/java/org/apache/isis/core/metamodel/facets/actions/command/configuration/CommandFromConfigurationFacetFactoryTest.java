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

package org.apache.isis.core.metamodel.facets.actions.command.configuration;

import java.lang.reflect.Method;
import java.util.UUID;

import org.jmock.Expectations;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.actions.command.CommandFacet;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacetAbstract;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.actions.command.CommandFacetAbstract;

public class CommandFromConfigurationFacetFactoryTest extends AbstractFacetFactoryJUnit4TestCase {

    private CommandFacetFromConfigurationFactory facetFactory;
    private Method customerActionMethod;

    @Before
    public void setUp() throws Exception {
        facetFactory = new CommandFacetFromConfigurationFactory();
        facetFactory.setConfiguration(mockConfiguration);

        customerActionMethod = findMethod(Customer.class, "someAction");
    }

    @After
    public void tearDown() throws Exception {
        facetFactory = null;
    }

    class Customer {
        public void someAction() {
        }
    }

    class SomeTransactionalId implements HasTransactionId {
        public void someAction() {
        }

        @Override
        public UUID getTransactionId() {
            return null;
        }

        @Override
        public void setTransactionId(UUID transactionId) {
        }
    }

    @Test(expected=IllegalStateException.class)
    public void requiresActionSemantics() {
        allowingConfigurationToReturn("safe");
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));
    }

    @Test
    public void ignoreHasTransactionId() {
        String configuredValue = "all";
        Of actionSemantics = Of.SAFE;

        Method actionMethod = findMethod(SomeTransactionalId.class, "someAction");

        allowingConfigurationToReturn(configuredValue);

        facetedMethod.addFacet(new ActionSemanticsFacetAbstract(actionSemantics, facetedMethod) {});
        facetFactory.process(new ProcessMethodContext(SomeTransactionalId.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CommandFacet.class);
        Assert.assertNull(facet);

        expectNoMethodsRemoved();
    }


    @Test
    public void all_with_safe() {
        String configuredValue = "all";
        Of actionSemantics = Of.SAFE;
        
        allowingConfigurationToReturn(configuredValue);

        facetedMethod.addFacet(new ActionSemanticsFacetAbstract(actionSemantics, facetedMethod) {});
        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, customerActionMethod, mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CommandFacet.class);
        Assert.assertTrue(facet instanceof CommandFacetAbstract);

        expectNoMethodsRemoved();
    }

    @Test
    public void ignoreQueryOnly_with_safe() {
        String configuredValue = "ignoreQueryOnly";
        Of actionSemantics = Of.SAFE;
        
        allowingConfigurationToReturn(configuredValue);

        facetedMethod.addFacet(new ActionSemanticsFacetAbstract(actionSemantics, facetedMethod) {});
        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, customerActionMethod, mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CommandFacet.class);
        Assert.assertNull(facet);

        expectNoMethodsRemoved();
    }

    @Test
    public void none_with_nonIdempotent() {
        String configuredValue = "none";
        Of actionSemantics = Of.NON_IDEMPOTENT;
        
        allowingConfigurationToReturn(configuredValue);

        facetedMethod.addFacet(new ActionSemanticsFacetAbstract(actionSemantics, facetedMethod) {});
        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, customerActionMethod, mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CommandFacet.class);
        Assert.assertNull(facet);

        expectNoMethodsRemoved();
    }

    private void allowingConfigurationToReturn(final String value) {
        context.checking(new Expectations() {
            {
                allowing(mockConfiguration).getString("isis.services.command.actions");
                will(returnValue(value));
            }
        });
    }


}
