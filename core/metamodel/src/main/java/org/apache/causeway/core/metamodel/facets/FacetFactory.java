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
import java.util.function.Function;
import java.util.stream.Stream;

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
import org.apache.causeway.core.metamodel.facetapi.FacetedMethod;
import org.apache.causeway.core.metamodel.facetapi.FacetedMethodParameter;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facetapi.MethodRemover;
import org.apache.causeway.core.metamodel.facetapi.MethodRemover.HasMethodRemover;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.impl._JUnitSupport;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;

/**
 * Responsible for processing elements of the metamodel, registered to the
 * {@link org.apache.causeway.core.metamodel.progmodel.ProgrammingModel} using
 * {@link org.apache.causeway.core.metamodel.progmodel.ProgrammingModel#addFactory(ProgrammingModel.FacetProcessingOrder, FacetFactory, ProgrammingModel.Marker...)}.
 *
 * <p>IMPORTANT: with respect to mixed-in members, {@link FacetFactory}s are
 * only run against those members in their original form as an action of
 * a mixin class, <i>not</i> as contributed mixin methods of the mixee type.
 * This is because they actually run against {@link FacetedMethod}s, which
 * are the peer object that is wrapped by (the respective subclasses of)
 * {@link org.apache.causeway.core.metamodel.spec.feature.ObjectMember}.
 *
 * <p>To process a mixin member in the context of it actually being a mixin
 * member (for example, authorization or translations), instead use the
 * {@link org.apache.causeway.core.metamodel.spec.impl.PostProcessor} interface.
 */
public interface FacetFactory {

	/**
	 * The {@link FeatureType}(s) this facet factory can create
	 * {@link Facet}(s) for.
	 *
	 * <p> Used to reduce the number of {@link FacetFactory}(s)
	 * that are queried when building up the meta-model.
	 */
	ImmutableEnumSet<FeatureType> getFeatureTypes();

	/**
	 * Process the class, and return the correctly setup annotation if present.
	 */
	void process(ProcessClassContext processClassContext);
	/**
	 * Process the method, and return the correctly setup annotation if present.
	 */
	void process(ProcessMethodContext processMethodContext);
	/**
	 * Process the parameters of the method, and return the correctly setup
	 * annotation if present.
	 */
	void processParams(ProcessParameterContext processParameterContext);

    interface ProcessWithClsContext<T extends FacetHolder> {
    	T facetHolder();

        /**
         * The class being introspected.
         *
         * <p>In the context of method introspection, this isn't necessarily the same as the
         * {@link java.lang.reflect.Method#getDeclaringClass() declaring class}
         * of the method being introspected; that method might have been inherited.
         */
    	Class<?> cls();

    	IntrospectionPolicy introspectionPolicy();

        /**
         * Annotation lookup on this context's type (cls).
         * @since 2.0
         */
        default <A extends Annotation> Optional<A> synthesizeOnType(final Class<A> annotationType) {
            return _Annotations.synthesize(cls(), annotationType);
        }
    }

    interface ProcessWithMethodContext<T extends FacetHolder>
    extends ProcessWithClsContext<T> {
    	MethodFacade methodFacade();

    	/**
    	 * Whether the method's underlying byte code was NOT compiled with the {@code -parameters} flag.
    	 * Might have false positives, hence 'potential' in the name.
    	 */
		default boolean hasPotentialNonReflectableParameterNames() {
			var methodFacade = methodFacade();
    		for(int i=0; i<methodFacade.getParameterCount(); ++i) {
    			var paramName = methodFacade.getParameterName(i);
    			if(paramName.equals("arg" + i)) return true;
    		}
    		return false;
    	}
    }

    // -- PROCESS CLASS

    public record ProcessClassContext(
    		Class<?> cls,
            IntrospectionPolicy introspectionPolicy,
            MethodRemover methodRemover,
            FacetHolder facetHolder,
            Function<Class<?>, ObjectSpecification> loadSpecificationTypeOnlyFunction)
    implements
    	ProcessWithClsContext<FacetHolder>, HasMethodRemover {

        // -- JUNIT SUPPORT

        /** For testing only. */
        public static ProcessClassContext forTesting(
                final Class<?> cls,
                final MethodRemover methodRemover,
                final FacetHolder facetHolder) {
            return new ProcessClassContext(
                    cls, IntrospectionPolicy.ANNOTATION_OPTIONAL, methodRemover, facetHolder,
                    _JUnitSupport.loadSpecificationTypeOnlyFunction(facetHolder.getSpecificationLoader()));
        }

        // -- SPEC

        public @Nullable ObjectSpecification loadSpecificationTypeOnly(@Nullable final Class<?> domainType) {
        	return loadSpecificationTypeOnlyFunction.apply(domainType);
        }
    }

    // -- PROCESS METHOD

    public record ProcessMethodContext(
    		 Class<?> cls,
             IntrospectionPolicy introspectionPolicy,
             FeatureType featureType,
             MethodFacade methodFacade,
             MethodRemover methodRemover,
             FacetedMethod facetedMethod,
             /**
              * Whether we are currently processing a mixin type AND this context's method can be identified
              * as the main method of the processed mixin class.
              * @since 2.0
              */
             boolean isMixinMain,
             Function<Class<?>, ObjectSpecification> loadSpecificationTypeOnlyFunction)
    implements
    	ProcessWithMethodContext<FacetedMethod>, HasMethodRemover {

    	@Override public FacetedMethod facetHolder() { return facetedMethod; }

        /**
         * Annotation lookup on this context's method. Also honors annotations on fields, if this method is a getter.
         * @since 2.0
         */
        public <A extends Annotation> Optional<A> synthesizeOnMethod(final Class<A> annotationType) {
            return methodFacade().synthesize(annotationType);
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
            return switch (featureType()) {
			case ACTION -> namingConventionForActionSupport(methodPrefix);
			case PROPERTY, COLLECTION -> isMixinMain()
			                        ? namingConventionForActionSupport(methodPrefix)
			                        : namingConventionForPropertyAndCollectionSupport(methodPrefix); // handles getters
			default -> Can.empty();
			};
        }

        public Can<java.util.function.IntFunction<String>> parameterSupportCandidates(
                final String methodPrefix) {

            return switch (featureType()) {
			case ACTION -> namingConventionForParameterSupport(methodPrefix);
			default -> Can.empty();
			};
        }

        // -- SUPPORTING METHOD NAMING CONVENTIONS

        private Can<String> namingConventionForActionSupport(
                final String prefix) {
            var actionMethod = methodFacade();
            return ProgrammingModelConstants.ActionSupportNaming
                    .namesFor(actionMethod, prefix, isMixinMain());
        }

        private Can<java.util.function.IntFunction<String>> namingConventionForParameterSupport(
                final String prefix) {
            var actionMethod = methodFacade();
            return ProgrammingModelConstants.ParameterSupportNaming
                    .namesFor(actionMethod, prefix, isMixinMain());
        }

        private Can<String> namingConventionForPropertyAndCollectionSupport(
                final String prefix) {
            var getterMethod = methodFacade();
            return ProgrammingModelConstants.MemberSupportNaming
                    .namesFor(getterMethod, prefix, isMixinMain());
        }

        // -- SPEC

		public @Nullable ObjectSpecification loadSpecificationTypeOnly(@Nullable final Class<?> domainType) {
        	return loadSpecificationTypeOnlyFunction.apply(domainType);
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
                    methodRemover, facetedMethod, false,
                    _JUnitSupport.loadSpecificationTypeOnlyFunction(facetedMethod.getSpecificationLoader()));
        }

    }

    // -- PROCESS PARAM

    public record ProcessParameterContext(
    		Class<?> cls,
            IntrospectionPolicy introspectionPolicy,
            MethodFacade methodFacade,
            MethodRemover methodRemover,
            FacetedMethodParameter facetedMethodParameter)
    implements
    	ProcessWithMethodContext<FacetedMethodParameter>, HasMethodRemover {

    	@Override public FacetedMethodParameter facetHolder() { return facetedMethodParameter; }

        public int paramNum() { return facetedMethodParameter.paramIndex(); }
		public Class<?> parameterType() { return methodFacade.getParameterType(paramNum()); }
		public String parameterName() { return methodFacade.getParameterName(paramNum()); }

        /**
         * Annotation lookup on this context's method parameter.
         * @since 2.0
         */
        public <A extends Annotation> Optional<A> synthesizeOnParameter(final Class<A> annotationType) {
            return methodFacade.synthesizeOnParameter(annotationType, paramNum());
        }

        public Stream<Annotation> streamParameterAnnotations() {
            var parameterTypeAnnotations = methodFacade.asExecutable()
                .getAnnotatedParameterTypes()[paramNum()]
                .getAnnotations();
            var parameterAnnotations = MethodParameter
                    .forExecutable(
                    		methodFacade.asExecutable(),
                            paramNum())
                    .getParameterAnnotations();
            return Stream.concat(
                _NullSafe.stream(parameterTypeAnnotations),
                _NullSafe.stream(parameterAnnotations));
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

}
