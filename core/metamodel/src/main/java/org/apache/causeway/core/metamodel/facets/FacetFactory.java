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

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.core.MethodParameter;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.reflection._Annotations;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facetapi.MethodRemover;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;

import lombok.Getter;
import org.jspecify.annotations.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Responsible for processing elements of the metamodel, registered to the
 * {@link org.apache.causeway.core.metamodel.progmodel.ProgrammingModel} using
 * {@link org.apache.causeway.core.metamodel.progmodel.ProgrammingModel#addFactory(ProgrammingModel.FacetProcessingOrder, FacetFactory, ProgrammingModel.Marker...)}.
 *
 * <p>
 *     IMPORTANT: with respect to mixed-in members, {@link FacetFactory}s are
 *     only run against those members in their original form as an action of
 *     a mixin class, <i>not</i> as contributed mixin methods of the mixee type.
 *     This is because they actually run against {@link FacetedMethod}s, which
 *     are the peer object that is wrapped by (the respective subclasses of)
 *     {@link org.apache.causeway.core.metamodel.spec.feature.ObjectMember}.
 * </p>
 *
 * <p>
 *     To process a mixin member in the context of it actually being a mixin
 *     member (for example, authorization or translations), instead use the
 *     {@link org.apache.causeway.core.metamodel.spec.impl.PostProcessor} interface.
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
         *
         * <p>
         *     In the context of method introspection, this isn't necessarily the same as the
         *     {@link java.lang.reflect.Method#getDeclaringClass() declaring class}
         *     of the method being introspected; that method might have been inherited.
         * </p>
         */
        @Getter private final @NonNull Class<?> cls;
        @Getter private final @NonNull IntrospectionPolicy introspectionPolicy;

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
            return _Annotations.synthesize(cls, annotationType);
        }

    }

    static class AbstractProcessWithMethodContext<T extends FacetHolder>
    extends AbstractProcessWithClsContext<T>
    implements MethodRemover{

        @Getter private final MethodFacade method;
        protected final MethodRemover methodRemover;

        AbstractProcessWithMethodContext(
                final Class<?> cls,
                final IntrospectionPolicy introspectionPolicy,
                final MethodFacade method,
                final MethodRemover methodRemover,
                final T facetHolder) {

            super(cls, introspectionPolicy, facetHolder);
            this.method = method;
            this.methodRemover = methodRemover;
        }

        @Override
        public void removeMethod(final ResolvedMethod method) {
            methodRemover.removeMethod(method);
        }

        @Override
        public void removeMethods(final Predicate<ResolvedMethod> filter, final Consumer<ResolvedMethod> onRemoval) {
            methodRemover.removeMethods(filter, onRemoval);
        }

        @Override
        public Can<ResolvedMethod> snapshotMethodsRemaining() {
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

    public static final class ProcessClassContext
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
        public void removeMethod(final @Nullable ResolvedMethod method) {
            methodRemover.removeMethod(method);
        }

        @Override
        public void removeMethods(final Predicate<ResolvedMethod> filter, final Consumer<ResolvedMethod> onRemoval) {
            methodRemover.removeMethods(filter, onRemoval);
        }

        @Override
        public Can<ResolvedMethod> snapshotMethodsRemaining() {
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

    public static final class ProcessMethodContext
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
         * @param isMixinMain whether we are currently processing a mixin type AND this context's method can be identified
         *         as the main method of the processed mixin class. (since 2.0)
         */
        public ProcessMethodContext(
                final Class<?> cls,
                final IntrospectionPolicy introspectionPolicy,
                final FeatureType featureType,
                final MethodFacade method,
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
            return getMethod().synthesize(annotationType);
        }

        /**
         * Annotation lookup on this context's method, if not found, extends search to type in case
         * the predicate {@link #isMixinMain} evaluates {@code true}.
         * <p>
         * As of [CAUSEWAY-2604] we also make sure the annotation type does not appear in both places
         * (method and type). Hence the 2nd parameter is a callback that fires if the annotation
         * is found in both places.
         *
         * @since 2.0
         */
        public <A extends Annotation> Optional<A> synthesizeOnMethodOrMixinType(
                final @NonNull Class<A> annotationType,
                final @NonNull Runnable onAmbiguity) {

            var onMethod = synthesizeOnMethod(annotationType);
            var onType = synthesizeOnType(annotationType);

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
            var actionMethod = getMethod();
            return ProgrammingModelConstants.ActionSupportNaming
                    .namesFor(actionMethod, prefix, isMixinMain());
        }

        private Can<java.util.function.IntFunction<String>> namingConventionForParameterSupport(
                final String prefix) {
            var actionMethod = getMethod();
            return ProgrammingModelConstants.ParameterSupportNaming
                    .namesFor(actionMethod, prefix, isMixinMain());
        }

        private Can<String> namingConventionForPropertyAndCollectionSupport(
                final String prefix) {
            var getterMethod = getMethod();
            return ProgrammingModelConstants.MemberSupportNaming
                    .namesFor(getterMethod, prefix, isMixinMain());
        }

        // -- JUNIT SUPPORT

        /**
         * JUnit support, historically using (classic) {@link IntrospectionPolicy#ANNOTATION_OPTIONAL}
         * and {@code isMixinMain=false}
         */
        public static ProcessMethodContext forTesting(
                final Class<?> cls,
                final FeatureType featureType,
                final ResolvedMethod method,
                final MethodRemover methodRemover,
                final FacetedMethod facetedMethod) {
            return new ProcessMethodContext(
                    cls, IntrospectionPolicy.ANNOTATION_OPTIONAL, featureType, _MethodFacades.regular(method),
                    methodRemover, facetedMethod, false);
        }

    }

    /**
     * Process the method, and return the correctly setup annotation if present.
     */
    void process(ProcessMethodContext processMethodContext);

    // -- PROCESS PARAM

    public static final class ProcessParameterContext
    extends AbstractProcessWithMethodContext<FacetedMethodParameter> {

        @Getter private final int paramNum;
        @Getter private final Class<?> parameterType;
        @Getter private final String parameterName;

        public ProcessParameterContext(
                final Class<?> cls,
                final IntrospectionPolicy introspectionPolicy,
                final MethodFacade method,
                final MethodRemover methodRemover,
                final FacetedMethodParameter facetedMethodParameter) {

            super(cls, introspectionPolicy, method, methodRemover, facetedMethodParameter);
            this.paramNum = facetedMethodParameter.getParamIndex();
            this.parameterType = super.method.getParameterType(paramNum);
            this.parameterName = super.method.getParameterName(paramNum);
        }

        /**
         * Annotation lookup on this context's method parameter.
         * @since 2.0
         */
        public <A extends Annotation> Optional<A> synthesizeOnParameter(final Class<A> annotationType) {
            return super.method.synthesizeOnParameter(annotationType, paramNum);
        }

        public Stream<Annotation> streamParameterAnnotations() {
            var parameterAnnotations = MethodParameter
                    .forExecutable(
                            this.getMethod().asExecutable(),
                            this.getParamNum())
                    .getParameterAnnotations();
            return _NullSafe.stream(parameterAnnotations);
        }

        //JUnit
        public static ProcessParameterContext forTesting(
                final Class<?> type, final IntrospectionPolicy annotationOptional,
                final ResolvedMethod method, final MethodRemover methodRemover,
                final FacetedMethodParameter facetedMethodParameter) {
            return new ProcessParameterContext(type, annotationOptional,
                    _MethodFacades.regular(method), methodRemover, facetedMethodParameter);
        }

    }

    /**
     * Process the parameters of the method, and return the correctly setup
     * annotation if present.
     */
    void processParams(ProcessParameterContext processParameterContext);

}
