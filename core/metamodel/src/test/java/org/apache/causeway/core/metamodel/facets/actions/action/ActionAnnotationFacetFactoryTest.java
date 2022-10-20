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
package org.apache.causeway.core.metamodel.facets.actions.action;

import java.lang.reflect.Method;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import org.apache.causeway.applib.mixins.system.HasInteractionId;
import org.apache.causeway.core.config.metamodel.facets.ActionConfigOptions;
import org.apache.causeway.core.metamodel.facets.AbstractFacetFactoryJupiterTestCase;
import org.apache.causeway.core.metamodel.facets.object.domainobject.domainevents.ActionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.val;

class ActionAnnotationFacetFactoryTest
extends AbstractFacetFactoryJupiterTestCase {

    ActionAnnotationFacetFactory facetFactory;
    Method actionMethod;

    ObjectSpecification mockTypeSpec;
    ObjectSpecification mockReturnTypeSpec;

    void expectRemoveMethod(final Method actionMethod) {
        Mockito.verify(mockMethodRemover, Mockito.atLeastOnce()).removeMethod(actionMethod);
    }

    @BeforeEach
    public void setUp() throws Exception {

        mockTypeSpec = Mockito.mock(ObjectSpecification.class);
        mockReturnTypeSpec = Mockito.mock(ObjectSpecification.class);

        facetFactory = new ActionAnnotationFacetFactory(metaModelContext);

        Mockito.when(mockTypeSpec.getFacet(ActionDomainEventDefaultFacetForDomainObjectAnnotation.class))
        .thenReturn(null);

        actionMethod = findMethod(Customer.class, "someAction");
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        facetFactory = null;
    }

    class Customer {
        public void someAction() {
        }
    }

    class SomeHasInteractionId implements HasInteractionId {
        public void someAction() {
        }

        @Override
        public UUID getInteractionId() {
            return null;
        }


    }

    void allowingPublishingConfigurationToReturn(final ActionConfigOptions.PublishingPolicy value) {
        val config = metaModelContext.getConfiguration();
        config.getApplib().getAnnotation().getAction().setExecutionPublishing(value);
    }

}
