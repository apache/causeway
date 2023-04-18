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
import java.util.function.BiConsumer;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import static org.mockito.Mockito.calls;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel._testing.MethodRemover_forTesting;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facetapi.MethodRemover;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.causeway.core.metamodel.valuesemantics.IntValueSemantics;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.Accessors;

public abstract class FacetFactoryTestAbstract
implements HasMetaModelContext {

    // -- SCENARIO HELPER

    @lombok.Value
    @Getter @Accessors(fluent=true)
    public static class Scenario {
        private final Class<?> declaringClass;
        private final String memberId;
        private final Method annotatedMethod;
        private final FacetHolder facetHolder;
        private final FacetedMethod facetedMethod;
        private final FacetedMethodParameter facetedMethodParameter;

        public static Scenario act(
                final @NonNull MetaModelContext mmc,
                final Class<?> declaringClass, final String memberId) {

            val facetHolder = facetHolder(mmc, declaringClass, memberId);
            val actionMethod = _Utils.findMethodByNameOrFail(declaringClass, memberId);
            val facetedMethod = FacetedMethod.createForAction(mmc, declaringClass, memberId, actionMethod.getParameterTypes());
            val facetedMethodParameter =
                    actionMethod.getParameterCount()==0
                    ? (FacetedMethodParameter)null
                    : new FacetedMethodParameter(mmc,
                        FeatureType.ACTION_PARAMETER_SINGULAR, facetedMethod.getOwningType(),
                        facetedMethod.getMethod(), 0);

            return new Scenario(declaringClass, memberId, actionMethod, facetHolder, facetedMethod, facetedMethodParameter);
        }

        public static Scenario prop(
                final @NonNull MetaModelContext mmc,
                final Class<?> declaringClass, final String memberId) {

            val facetHolder = facetHolder(mmc, declaringClass, memberId);
            val getter = _Utils.findGetterOrFail(declaringClass, memberId);
            val facetedMethod = FacetedMethod.createForProperty(mmc, declaringClass, getter);
            val facetedMethodParameter = (FacetedMethodParameter)null;
            return new Scenario(declaringClass, memberId, getter, facetHolder, facetedMethod, facetedMethodParameter);
        }

        private static FacetHolder facetHolder(
                final @NonNull MetaModelContext mmc,
                final Class<?> declaringClass, final String memberId) {
            return FacetHolder.simple(mmc,
                    Identifier.propertyIdentifier(LogicalType.fqcn(declaringClass), memberId));
        }

    }

    // --

    @Getter(onMethod_ = {@Override})
    private MetaModelContext metaModelContext;
    private MethodRemover mockMethodRemover;

    /**
     * Override, if a custom {@link MetaModelContext_forTesting} is required for certain tests.
     */
    protected MetaModelContext_forTesting setUpMmc(
            final MetaModelContext_forTesting.MetaModelContext_forTestingBuilder builder) {
        return builder.build();
    }

    @BeforeEach
    protected void setUpAll() throws Exception {
        metaModelContext = setUpMmc(MetaModelContext_forTesting.builder()
                .valueSemantic(new IntValueSemantics()));
        mockMethodRemover = Mockito.mock(MethodRemover.class);
    }


    @FunctionalInterface
    public static interface MemberScenarioConsumer {
        void accept(
                ProcessMethodContext processMethodContext,
                FacetHolder facetHolder,
                FacetedMethod facetedMethod,
                FacetedMethodParameter facetedMethodParameter);
    }

    @FunctionalInterface
    public static interface ParameterScenarioConsumer {
        void accept(
                ProcessParameterContext processParameterContext,
                FacetHolder facetHolder,
                FacetedMethod facetedMethod,
                FacetedMethodParameter facetedMethodParameter);
    }

    /**
     * Action scenario.
     */
    protected void actionScenario(
            final Class<?> declaringClass, final String actionName, final MemberScenarioConsumer consumer) {
        val scenario = Scenario.act(getMetaModelContext(), declaringClass, actionName);
        val processMethodContext = ProcessMethodContext
                .forTesting(declaringClass, null, scenario.annotatedMethod(), mockMethodRemover, scenario.facetedMethod());
        consumer.accept(processMethodContext, scenario.facetHolder, scenario.facetedMethod, scenario.facetedMethodParameter);
    }

    /**
     * Action Parameter scenario.
     */
    protected void parameterScenario(
            final Class<?> declaringClass, final String actionName, final int paramIndex,
            final ParameterScenarioConsumer consumer) {
        _Assert.assertEquals(0, paramIndex, ()->"not yet implemented otherwise");
        val scenario = Scenario.act(getMetaModelContext(), declaringClass, actionName);
        val processParameterContext =
                FacetFactory.ProcessParameterContext.forTesting(
                        declaringClass, IntrospectionPolicy.ANNOTATION_OPTIONAL, scenario.annotatedMethod(), null, scenario.facetedMethodParameter());
        consumer.accept(processParameterContext, scenario.facetHolder, scenario.facetedMethod, scenario.facetedMethodParameter);
    }

    /**
     * Property scenario.
     */
    protected void propertyScenario(
            final Class<?> declaringClass, final String propertyName, final MemberScenarioConsumer consumer) {
        val scenario = Scenario.prop(getMetaModelContext(), declaringClass, propertyName);
        val processMethodContext = ProcessMethodContext
                .forTesting(declaringClass, null, scenario.annotatedMethod(), mockMethodRemover, scenario.facetedMethod());
        consumer.accept(processMethodContext, scenario.facetHolder, scenario.facetedMethod, scenario.facetedMethodParameter);
    }

    /**
     * DomainObject scenario.
     */
    protected void objectScenario(final Class<?> declaringClass, final BiConsumer<ProcessClassContext, FacetHolder> consumer) {
        val facetHolder = FacetHolder.simple(getMetaModelContext(),
                Identifier.classIdentifier(LogicalType.fqcn(declaringClass)));
        val processClassContext = ProcessClassContext
                .forTesting(declaringClass, mockMethodRemover, facetHolder);
        consumer.accept(processClassContext, facetHolder);
    }

    protected MethodRemover defaultMethodRemover() {
        return new MethodRemover_forTesting();
    }

    protected FacetedMethod facetedSetter(final Class<?> declaringClass, final String propertyName) {
        return FacetedMethod.createSetterForProperty(getMetaModelContext(),
                declaringClass, propertyName);
    }

    protected FacetedMethod facetedAction(final Class<?> declaringClass, final String methodName) {
        return FacetedMethod.createForAction(getMetaModelContext(),
                declaringClass, methodName);
    }

    protected boolean contains(final Class<?>[] types, final Class<?> type) {
        return _Utils.contains(types, type);
    }

    protected static boolean contains(final ImmutableEnumSet<FeatureType> featureTypes, final FeatureType featureType) {
        return _Utils.contains(featureTypes, featureType);
    }

    protected Method findMethod(final Class<?> type, final String methodName, final Class<?>[] methodTypes) {
        return _Utils.findMethodExactOrFail(type, methodName, methodTypes);
    }

    protected Method findMethod(final Class<?> type, final String methodName) {
        return _Utils.findMethodExactOrFail(type, methodName);
    }

    // -- EXPECTATIONS

    protected void expectNoMethodsRemoved() {
        Mockito.verifyNoInteractions(mockMethodRemover);
    }

    protected void expectRemoveMethodAtLeastOnce(final Method actionMethod) {
        Mockito.verify(mockMethodRemover, Mockito.atLeastOnce()).removeMethod(actionMethod);
    }

    protected void expectRemoveMethodOnce(final Method actionMethod) {
        Mockito.verify(mockMethodRemover, calls(1)).removeMethod(actionMethod);
    }


}
