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
package org.apache.isis.core.metamodel.facets;

import java.lang.reflect.Method;
import java.util.Optional;

import org.jmock.Expectations;
import org.junit.Rule;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.iactn.InteractionProvider;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel._testing.MethodRemover_forTesting;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderAbstract;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.actions.layout.ActionLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.layout.CollectionLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.PropertyLayoutFacetFactory;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.security.authentication.InteractionContextFactory;

import junit.framework.TestCase;

public abstract class AbstractFacetFactoryTest extends TestCase {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

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
    protected InteractionProvider mockInteractionProvider;
    protected final InteractionContext iaContext = InteractionContextFactory.testing();
    protected SpecificationLoader mockSpecificationLoader;
    protected MethodRemover_forTesting methodRemover;

    protected FacetHolder facetHolder;
    protected FacetedMethod facetedMethod;
    protected FacetedMethodParameter facetedMethodParameter;
    protected MetaModelContext_forTesting metaModelContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // PRODUCTION

        methodRemover = new MethodRemover_forTesting();

        mockInteractionProvider = context.mock(InteractionProvider.class);

        mockTranslationService = context.mock(TranslationService.class);

        mockSpecificationLoader = context.mock(SpecificationLoader.class);

        metaModelContext = MetaModelContext_forTesting.builder()
                .specificationLoader(mockSpecificationLoader)
                .translationService(mockTranslationService)
                .interactionProvider(mockInteractionProvider)
                .build();

        context.checking(new Expectations() {{

            allowing(mockInteractionProvider).currentInteractionContext();
            will(returnValue(Optional.of(iaContext)));
        }});

        facetHolder = FacetHolderAbstract.simple(
                metaModelContext,
                Identifier.propertyOrCollectionIdentifier(LogicalType.fqcn(Customer.class), "firstName"));

        facetedMethod = FacetedMethod.createForProperty(metaModelContext, Customer.class, "firstName");
        facetedMethodParameter = new FacetedMethodParameter(
                metaModelContext,
                FeatureType.ACTION_PARAMETER_SCALAR, facetedMethod.getOwningType(), facetedMethod.getMethod(), String.class
                );
    }



    protected void allowing_specificationLoader_loadSpecification_any_willReturn(final ObjectSpecification objectSpecification) {
        context.checking(new Expectations() {{
            allowing(mockSpecificationLoader).loadSpecification(with(any(Class.class)));
            will(returnValue(objectSpecification));
        }});
    }

    @Override
    protected void tearDown() throws Exception {
        mockSpecificationLoader = null;
        methodRemover = null;
        facetedMethod = null;
        super.tearDown();
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

    // -- FACTORIES

    protected static PropertyLayoutFacetFactory createPropertyLayoutFacetFactory(MetaModelContext mmc) {
        return new PropertyLayoutFacetFactory(mmc) {
            @Override
            public IsisConfiguration getConfiguration() {
                return new IsisConfiguration(null);
            }
        };
    }

    protected static CollectionLayoutFacetFactory createCollectionLayoutFacetFactory(MetaModelContext mmc) {
        return new CollectionLayoutFacetFactory(mmc) {
            @Override
            public IsisConfiguration getConfiguration() {
                return new IsisConfiguration(null);
            }
        };
    }

    protected static ActionLayoutFacetFactory createActionLayoutFacetFactory(MetaModelContext mmc) {
        return new ActionLayoutFacetFactory(mmc) {
            @Override
            public IsisConfiguration getConfiguration() {
                return new IsisConfiguration(null);
            }
        };
    }


}
