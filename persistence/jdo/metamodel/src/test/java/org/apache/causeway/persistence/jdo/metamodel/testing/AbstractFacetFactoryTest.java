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
package org.apache.causeway.persistence.jdo.metamodel.testing;

import java.lang.reflect.Method;
import java.util.Optional;

import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.repository.EntityState;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.core.config.beans.PersistenceStack;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel._testing.MethodRemover_forTesting;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.FacetedMethodParameter;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.security.authentication.InteractionContextFactory;
import org.apache.causeway.persistence.jdo.provider.entities.JdoFacetContext;

public abstract class AbstractFacetFactoryTest {

    public static class Customer {

        private String firstName;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(final String firstName) {
            this.firstName = firstName;
        }
    }

    protected TranslationService mockTranslationService;
    protected InteractionService mockInteractionService;
    protected final InteractionContext iaContext = InteractionContextFactory.testing();
    protected SpecificationLoader mockSpecificationLoader;
    protected MethodRemover_forTesting methodRemover;

    protected FacetHolder facetHolder;
    protected FacetedMethod facetedMethod;
    protected FacetedMethodParameter facetedMethodParameter;
    protected MetaModelContext_forTesting metaModelContext;
    protected JdoFacetContext jdoFacetContext;

    protected void setUp() throws Exception {

        methodRemover = new MethodRemover_forTesting();

        mockInteractionService = Mockito.mock(InteractionService.class);
        mockTranslationService = Mockito.mock(TranslationService.class);
        mockSpecificationLoader = Mockito.mock(SpecificationLoader.class);

        metaModelContext = MetaModelContext_forTesting.builder()
                .specificationLoader(mockSpecificationLoader)
                .translationService(mockTranslationService)
                .interactionService(mockInteractionService)
                .build();

        Mockito
        .when(mockInteractionService.currentInteractionContext())
        .thenReturn(Optional.of(iaContext));

        facetHolder = FacetHolder.simple(
                metaModelContext,
                Identifier.propertyIdentifier(LogicalType.fqcn(Customer.class), "firstName"));

        facetedMethod = FacetedMethod.createSetterForProperty(metaModelContext, Customer.class, "firstName");
        facetedMethodParameter = new FacetedMethodParameter(
                metaModelContext,
                FeatureType.ACTION_PARAMETER_SINGULAR, facetedMethod.getOwningType(),
                facetedMethod.getMethod(), 0);

        jdoFacetContext = jdoFacetContextForTesting();
    }

    protected void tearDown() throws Exception {
        mockSpecificationLoader = null;
        methodRemover = null;
        facetedMethod = null;
    }

    protected static boolean contains(final Class<?>[] types, final Class<?> type) {
        return Utils.contains(types, type);
    }

    protected static boolean contains(final ImmutableEnumSet<FeatureType> featureTypes, final FeatureType featureType) {
        return Utils.contains(featureTypes, featureType);
    }

    protected static Method findMethod(final Class<?> type, final String methodName, final Class<?>[] methodTypes) {
        return Utils.findMethod(type, methodName, methodTypes);
    }

    protected Method findMethod(final Class<?> type, final String methodName) {
        return Utils.findMethod(type, methodName);
    }

    protected void assertNoMethodsRemoved() {
        assertTrue(methodRemover.getRemovedMethodMethodCalls().isEmpty());
        assertTrue(methodRemover.getRemoveMethodArgsCalls().isEmpty());
    }

    public static JdoFacetContext jdoFacetContextForTesting() {
        return new JdoFacetContext() {
            @Override public boolean isPersistenceEnhanced(final Class<?> cls) {
                return true;
            }
            @Override public boolean isMethodProvidedByEnhancement(final Method method) {
                return false;
            }
            @Override public EntityState getEntityState(final Object pojo) {
                return null;
            }
            @Override
            public EntityFacet createEntityFacet(final FacetHolder facetHolder, final Class<?> entityClass) {
                return EntityFacet.forTesting(PersistenceStack.JDO, facetHolder);
            }
        };
    }

}
