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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.reflection._Annotations;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Responsible for processing elements of the metamodel, registered to the
 * {@link org.apache.isis.core.metamodel.progmodel.ProgrammingModel} using
 * {@link org.apache.isis.core.metamodel.progmodel.ProgrammingModel#addFactory(ProgrammingModel.FacetProcessingOrder, FacetFactory, ProgrammingModel.Marker...)}.
 *
 * <p>
 *     IMPORTANT: with respect to mixed-in members, {@link FacetFactory}s are
 *     only run against those members in their original form as an action of
 *     a mixin class, <i>not</i> as contributed mixin methods of the mixee type.
 *     This is because they actually run against {@link FacetedMethod}s, which
 *     are the peer object that is wrapped by (the respective subclasses of)
 *     {@link org.apache.isis.core.metamodel.spec.feature.ObjectMember}.
 * </p>
 *
 * <p>
 *     To process a mixin member in the context of it actually being a mixin
 *     member (for example, authorization or translations), instead use the
 *     {@link org.apache.isis.core.metamodel.specloader.postprocessor.PostProcessor} interface.
 * </p>
 */
public interface FacetFactory {

    @RequiredArgsConstructor
    static class AbstractProcessContext<T extends FacetHolder> {
        @Getter private final T facetHolder;
    }

    static class AbstractProcessWithClsContext<T extends FacetHolder>
    extends AbstractProcessContext<T>{

        /**
         * The class being introspected.
         * <p>
         *     In the context of method introspection, this isn't necessarily the same as the
         *     {@link java.lang.reflect.Method#getDeclaringClass() declaring class}
         *     of the {@link #getMethod() method}; the method might have been inherited.
         * </p>
         */
        @Getter private final Class<?> cls;
        @Getter private final IntrospectionPolicy introspectionPolicy;

        AbstractProcessWithClsContext(
                final Class<?> cls,
                final IntrospectionPolicy introspectionPolicy,
                final T facetHolder) {
            super(facetHolder);
            this.cls = cls;
            this.introspectionPolicy = introspectionPolicy;
        }

        /**
         * Annotation lookup on this context's type (cls).
         * @since 2.0
         */
        public <A extends Annotation> Optional<A> synthesizeOnType(final Class<A> annotationType) {
            return _Annotations.synthesizeInherited(cls, annotationType);
        }

    }

    static class AbstractProcessWithMethodContext<T extends FacetHolder>
    extends AbstractProcessWithClsContext<T>
    implements MethodRemover{

        @Getter private final Method method;
        protected final MethodRemover methodRemover;

        AbstractProcessWithMethodContext(
                final Class<?> cls,
                final IntrospectionPolicy introspectionPolicy,
                final Method method,
                final MethodRemover methodRemover,
                final T facetHolder) {

            super(cls, introspectionPolicy, facetHolder);
            this.method = method;
            this.methodRemover = methodRemover;
        }

        @Override
        public void removeMethod(final Method method) {
            methodRemover.removeMethod(method);
        }

        @Override
        public void removeMethods(final Predicate<Method> filter, final Consumer<Method> onRemoval) {
            methodRemover.removeMethods(filter, onRemoval);
        }

        @Override
        public Can<Method> snapshotMethodsRemaining() {
            return methodRemover.snapshotMethodsRemaining();
        }

    }

    public interface ProcessContextWithMetadataProperties<T extends FacetHolder> {
        public T getFacetHolder();
    }

    /**
     * The {@link FeatureType feature type}s that this facet factory can create
     * {@link Facet}s for.
     *
     * <p>
     * Used by the Java 8 Reflector's <tt>ProgrammingModel</tt> to reduce the
     * number of {@link FacetFactory factory}s that are queried when building up
     * the meta-model.
     *
     */
    ImmutableEnumSet<FeatureType> getFeatureTypes();


    // //////////////////////////////////////
    // process class
    // //////////////////////////////////////

    public static class ProcessClassContext
    extends AbstractProcessWithClsContext<FacetHolder>
    implements MethodRemover, ProcessContextWithMetadataProperties<FacetHolder> {

        private final MethodRemover methodRemover;

        public ProcessClassContext(
                final Class<?> cls,
                final IntrospectionPolicy introspectionPolicy,
                final MethodRemover methodRemover,
                final FacetHolder facetHolder) {
            super(cls, introspectionPolicy, facetHolder);
            this.methodRemover = methodRemover;
        }

        @Override
        public void removeMethod(final @Nullable Method method) {
            methodRemover.removeMethod(method);
        }

        @Override
        public void removeMethods(final Predicate<Method> filter, final Consumer<Method> onRemoval) {
            methodRemover.removeMethods(filter, onRemoval);
        }

        @Override
        public Can<Method> snapshotMethodsRemaining() {
            return methodRemover.snapshotMethodsRemaining();
        }

        // -- JUNIT SUPPORT

        /**
         * For testing only.
         */
        public static ProcessClassContext forTesting(
                final Class<?> cls,
                final MethodRemover methodRemover,
                final FacetHolder facetHolder) {
            return new ProcessClassContext(
                    cls, IntrospectionPolicy.ANNOTATION_OPTIONAL, methodRemover, facetHolder);
        }

    }

    /**
     * Process the class, and return the correctly setup annotation if present.
     */
    void process(ProcessClassContext processClassContext);

    // //////////////////////////////////////
    // process method
    // //////////////////////////////////////


    public static class ProcessMethodContext
    extends AbstractProcessWithMethodContext<FacetedMethod>
    implements ProcessContextWithMetadataProperties<FacetedMethod> {

        @Getter private final FeatureType featureType;
        /**
         * Whether we are currently processing a mixin type AND this context's method can be identified
         * as the main method of the processed mixin class.
         * @since 2.0
         */
        @Getter private final boolean mixinMain;

        /**
         * @param cls
         * @param featureType
         * @param method
         * @param methodRemover
         * @param facetedMethod
         * @param isMixinMain
         *       - Whether we are currently processing a mixin type AND this context's method can be identified
         *         as the main method of the processed mixin class. (since 2.0)
         */
        public ProcessMethodContext(
                final Class<?> cls,
                final IntrospectionPolicy introspectionPolicy,
                final FeatureType featureType,
                final Method method,
                final MethodRemover methodRemover,
                final FacetedMethod facetedMethod,
                final boolean isMixinMain) {

            super(cls, introspectionPolicy, method, methodRemover, facetedMethod);
            this.featureType = featureType;
            this.mixinMain = isMixinMain;
        }

        /**
         * Annotation lookup on this context's method. Also honors annotations on fields, if this method is a getter.
         * @since 2.0
         */
        public <A extends Annotation> Optional<A> synthesizeOnMethod(final Class<A> annotationType) {
            return _Annotations.synthesizeInherited(getMethod(), annotationType);
        }

        /**
         * Annotation lookup on this context's method, if not found, extends search to type in case
         * the predicate {@link #isMixinMain} evaluates {@code true}.
         * <p>
         * As of [ISIS-2604] we also make sure the annotation type does not appear in both places
         * (method and type). Hence the 2nd parameter is a callback that fires if the annotation
         * is found in both places.
         *
         * @since 2.0
         */
        public <A extends Annotation> Optional<A> synthesizeOnMethodOrMixinType(
                final @NonNull Class<A> annotationType,
                final @NonNull Runnable onAmbiguity) {


            val onMethod = synthesizeOnMethod(annotationType);
            val onType = synthesizeOnType(annotationType);

            if(onMethod.isPresent()) {
                if(onType.isPresent()) {
                    onAmbiguity.run();
                }
                return onMethod;
            }
            return onType;
        }

        public Can<String> memberSupportCandidates(
                final String methodPrefix) {
            switch(getFeatureType()) {
            case ACTION:
                return namingConventionForActionSupport(methodPrefix);
            case PROPERTY:
            case COLLECTION:
                return isMixinMain()
                        ? namingConventionForActionSupport(methodPrefix)
                        : namingConventionForPropertyAndCollectionSupport(methodPrefix); // handles getters

                //return namingConventionForPropertyAndCollectionSupport(methodPrefix);
            default:
                return Can.empty();
            }
        }

        public Can<java.util.function.IntFunction<String>> parameterSupportCandidates(
                final String methodPrefix) {

            switch(getFeatureType()) {
            case ACTION:
                return namingConventionForParameterSupport(methodPrefix);
            default:
                return Can.empty();
            }
        }


        // -- SUPPORTING METHOD NAMING CONVENTIONS

        private Can<String> namingConventionForActionSupport(
                final String prefix) {
            val actionMethod = getMethod();
            return ProgrammingModelConstants.ActionSupportNaming
                    .namesFor(actionMethod, prefix, isMixinMain());
        }

        private Can<java.util.function.IntFunction<String>> namingConventionForParameterSupport(
                final String prefix) {
            val actionMethod = getMethod();
            return ProgrammingModelConstants.ParameterSupportNaming
                    .namesFor(actionMethod, prefix, isMixinMain());
        }

        private Can<String> namingConventionForPropertyAndCollectionSupport(
                final String prefix) {
            val getterMethod = getMethod();
            return ProgrammingModelConstants.MemberSupportNaming
                    .namesFor(getterMethod, prefix, isMixinMain());
        }

        // -- JUNIT SUPPORT

        /**
         * JUnit support, historically using classic IntrospectionPolicy ANNOTATION_OPTIONAL
         *  and not using 'isMixinMain'
         */
        public static ProcessMethodContext forTesting(
                final Class<?> cls,
                final FeatureType featureType,
                final Method method,
                final MethodRemover methodRemover,
                final FacetedMethod facetedMethod) {
            return new ProcessMethodContext(
                    cls, IntrospectionPolicy.ANNOTATION_OPTIONAL, featureType, method,
                    methodRemover, facetedMethod, false);
        }

    }


    /**
     * Process the method, and return the correctly setup annotation if present.
     */
    void process(ProcessMethodContext processMethodContext);

    // -- PROCESS PARAM

    public static class ProcessParameterContext
    extends AbstractProcessWithMethodContext<FacetedMethodParameter> {

        @Getter private final int paramNum;
        @Getter private final Class<?> parameterType;
        @Getter private final Parameter parameter;

        public ProcessParameterContext(
                final Class<?> cls,
                final IntrospectionPolicy introspectionPolicy,
                final Method method,
                final MethodRemover methodRemover,
                final FacetedMethodParameter facetedMethodParameter) {

            super(cls, introspectionPolicy, method, methodRemover, facetedMethodParameter);
            this.paramNum = facetedMethodParameter.getParamIndex();
            this.parameterType = super.method.getParameterTypes()[paramNum];
            this.parameter = super.method.getParameters()[paramNum];
        }

        /**
         * Annotation lookup on this context's method parameter.
         * @since 2.0
         */
        public <A extends Annotation> Optional<A> synthesizeOnParameter(final Class<A> annotationType) {
            return _Annotations.synthesizeInherited(parameter, annotationType);
        }

    }

    /**
     * Process the parameters of the method, and return the correctly setup
     * annotation if present.
     */
    void processParams(ProcessParameterContext processParameterContext);


}
