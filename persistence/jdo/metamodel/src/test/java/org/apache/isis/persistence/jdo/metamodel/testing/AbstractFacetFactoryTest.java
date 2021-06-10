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

package org.apache.isis.persistence.jdo.metamodel.testing;

import java.lang.reflect.Method;
import java.util.Optional;

import org.jmock.Expectations;
import org.junit.Rule;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel._testing.MethodRemoverForTesting;
import org.apache.isis.core.metamodel.context.MetaModelContextAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.security.authentication.Authentication;
import org.apache.isis.core.security.authentication.AuthenticationProvider;
import org.apache.isis.persistence.jdo.provider.entities.JdoFacetContext;

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
    protected AuthenticationProvider mockAuthenticationProvider;
    protected Authentication mockAuthentication;
    protected SpecificationLoader mockSpecificationLoader;
    protected MethodRemoverForTesting methodRemover;

    protected FacetHolder facetHolder;
    protected FacetedMethod facetedMethod;
    protected FacetedMethodParameter facetedMethodParameter;
    protected MetaModelContext_forTesting metaModelContext;
    protected JdoFacetContext jdoFacetContext;

    public static class IdentifiedHolderImpl extends FacetHolderImpl implements IdentifiedHolder {

        private Identifier identifier;

        public IdentifiedHolderImpl(final Identifier identifier) {
            this.identifier = identifier;
        }

        @Override
        public Identifier getIdentifier() {
            return identifier;
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // PRODUCTION

        facetHolder = new IdentifiedHolderImpl(
                Identifier.propertyOrCollectionIdentifier(LogicalType.fqcn(Customer.class), "firstName"));
        facetedMethod = FacetedMethod.createForProperty(Customer.class, "firstName");
        facetedMethodParameter = new FacetedMethodParameter(
                FeatureType.ACTION_PARAMETER_SCALAR, facetedMethod.getOwningType(), facetedMethod.getMethod(), String.class
                );

        methodRemover = new MethodRemoverForTesting();

        mockAuthenticationProvider = context.mock(AuthenticationProvider.class);

        mockTranslationService = context.mock(TranslationService.class);
        mockAuthentication = context.mock(Authentication.class);

        mockSpecificationLoader = context.mock(SpecificationLoader.class);

        metaModelContext = MetaModelContext_forTesting.builder()
                .specificationLoader(mockSpecificationLoader)
                .translationService(mockTranslationService)
                .authenticationProvider(mockAuthenticationProvider)
                .build();

        context.checking(new Expectations() {{

            allowing(mockAuthenticationProvider).currentAuthentication();
            will(returnValue(Optional.of(mockAuthentication)));
        }});

        ((MetaModelContextAware)facetHolder).setMetaModelContext(metaModelContext);
        facetedMethod.setMetaModelContext(metaModelContext);
        facetedMethodParameter.setMetaModelContext(metaModelContext);

        jdoFacetContext = jdoFacetContextForTesting();
    }



    protected void allowing_specificationLoader_loadSpecification_any_willReturn(final ObjectSpecification objectSpecification) {
        context.checking(new Expectations() {{
            allowing(mockSpecificationLoader).specForType(with(any(Class.class)));
            will(returnValue(Optional.of(objectSpecification)));
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

    public static JdoFacetContext jdoFacetContextForTesting() {
        return new JdoFacetContext() {
            @Override public boolean isPersistenceEnhanced(Class<?> cls) {
                return true;
            }
            @Override public boolean isMethodProvidedByEnhancement(Method method) {
                return false;
            }
            @Override public EntityState getEntityState(Object pojo) {
                return null;
            }
        };
    }

}
