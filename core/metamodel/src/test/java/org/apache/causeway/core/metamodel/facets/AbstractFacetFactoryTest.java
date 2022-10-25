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
package org.apache.causeway.core.metamodel.facets;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel._testing.MethodRemover_forTesting;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.causeway.core.metamodel.facets.actions.layout.ActionLayoutFacetFactory;
import org.apache.causeway.core.metamodel.facets.collections.layout.CollectionLayoutFacetFactory;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.PropertyLayoutFacetFactory;
import org.apache.causeway.core.security.authentication.InteractionContextFactory;

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

    @Mock protected TranslationService mockTranslationService;
    @Mock protected InteractionService mockInteractionService;
    protected final InteractionContext iaContext = InteractionContextFactory.testing();
    protected MethodRemover_forTesting methodRemover;

    protected FacetHolder facetHolder;
    protected FacetedMethod facetedMethod;
    protected FacetedMethodParameter facetedMethodParameter;
    protected MetaModelContext_forTesting metaModelContext;

    @BeforeEach
    protected void setUp() throws Exception {

        methodRemover = new MethodRemover_forTesting();

        metaModelContext = MetaModelContext_forTesting.builder()
                .translationService(mockTranslationService)
                .interactionService(mockInteractionService)
                .build();

        Mockito.when(mockInteractionService.currentInteractionContext()).thenReturn(Optional.of(iaContext));

        facetHolder = FacetHolder.simple(
                metaModelContext,
                Identifier.propertyIdentifier(LogicalType.fqcn(Customer.class), "firstName"));

        facetedMethod = FacetedMethod.createSetterForProperty(metaModelContext, Customer.class, "firstName");
        facetedMethodParameter = new FacetedMethodParameter(
                metaModelContext,
                FeatureType.ACTION_PARAMETER_SINGULAR,
                facetedMethod.getOwningType(),
                facetedMethod.getMethod(), 0);
    }

    @AfterEach
    protected void tearDown() throws Exception {
        methodRemover = null;
        facetedMethod = null;
    }

    protected static boolean contains(final Class<?>[] types, final Class<?> type) {
        return Utils.contains(types, type);
    }

    protected static boolean contains(final ImmutableEnumSet<FeatureType> featureTypes, final FeatureType featureType) {
        return Utils.contains(featureTypes, featureType);
    }

    protected static Method findMethod(final Class<?> type, final String methodName, final Class<?>[] signature) {
        return Utils.findMethod(type, methodName, signature);
    }

    protected Method findMethod(final Class<?> type, final String methodName) {
        return Utils.findMethod(type, methodName);
    }

    protected void processMethod(
            final FacetFactory facetFactory,
            final Class<?> type,
            final String methodName,
            final Class<?>[] signature) {

        facetFactory.process(ProcessMethodContext
                .forTesting(type, null,
                        findMethod(type, methodName, signature),
                        methodRemover, facetedMethod));
    }

    protected void processParams(
            final FacetFactory facetFactory,
            final Class<?> type,
            final String methodName,
            final Class<?>[] signature) {

        facetFactory.processParams(new FacetFactory
                .ProcessParameterContext(type, IntrospectionPolicy.ANNOTATION_OPTIONAL,
                        findMethod(type, methodName, signature),
                        null, facetedMethodParameter));
    }

    protected void assertNoMethodsRemoved() {
        assertTrue(methodRemover.getRemovedMethodMethodCalls().isEmpty());
        assertTrue(methodRemover.getRemoveMethodArgsCalls().isEmpty());
    }

    // -- FACTORIES

    protected static PropertyLayoutFacetFactory createPropertyLayoutFacetFactory(final MetaModelContext mmc) {
        return new PropertyLayoutFacetFactory(mmc);
    }

    protected static CollectionLayoutFacetFactory createCollectionLayoutFacetFactory(final MetaModelContext mmc) {
        return new CollectionLayoutFacetFactory(mmc);
    }

    protected static ActionLayoutFacetFactory createActionLayoutFacetFactory(final MetaModelContext mmc) {
        return new ActionLayoutFacetFactory(mmc);
    }


}
