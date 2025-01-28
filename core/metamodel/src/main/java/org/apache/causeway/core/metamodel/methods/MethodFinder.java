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
package org.apache.causeway.core.metamodel.methods;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.annotation.Domain;
import org.apache.causeway.applib.annotation.Introspection.EncapsulationPolicy;
import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.functions._Predicates;
import org.apache.causeway.commons.internal.reflection._Annotations;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.ConflictingAnnotations;
import org.apache.causeway.core.metamodel.commons.MethodUtil;

import lombok.AccessLevel;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MethodFinder {

    public static MethodFinder of(
            final @NonNull Class<?> correspondingClass,
            final @NonNull Can<String> methodNameCandidatesPossiblyDuplicated,
            final @NonNull EncapsulationPolicy encapsulationPolicy,
            final @NonNull Predicate<ResolvedMethod> mustSatisfy) {

        final Predicate<ResolvedMethod> isNotStatic = MethodUtil::isNotStatic;
        var methodNameCandidates = methodNameCandidatesPossiblyDuplicated.distinct();

        return new MethodFinder(
                correspondingClass,
                encapsulationPolicy,
                methodNameCandidates.equals(ANY_NAME)
                        ? isNotStatic.and(mustSatisfy)
                        : isNotStatic
                            .and(method->methodNameCandidates.contains(method.name()))
                            .and(mustSatisfy),
                methodNameCandidates);
    }

    public static final Can<String> ANY_NAME = Can.of(""); // arbitrary marker
    public static final Class<?>[] NO_ARG = new Class<?>[0];

    public static MethodFinder notNecessarilyPublic(
            final Class<?> correspondingClass,
            final Can<String> methodNameCandidates) {
        return of(
                correspondingClass,
                methodNameCandidates,
                EncapsulationPolicy.ENCAPSULATED_MEMBERS_SUPPORTED,
                _Predicates.alwaysTrue()
                );
    }

    public static MethodFinder publicOnly(
            final Class<?> correspondingClass,
            final Can<String> methodNameCandidates) {
        return of(
                correspondingClass,
                methodNameCandidates,
                EncapsulationPolicy.ONLY_PUBLIC_MEMBERS_SUPPORTED,
                _Predicates.alwaysTrue()
                );
    }

    public static MethodFinder accessor(
            final Class<?> correspondingClass,
            final Can<String> methodNameCandidates,
            final IntrospectionPolicy memberIntrospectionPolicy) {
        return havingAnyOrNoAnnotation(
                correspondingClass,
                methodNameCandidates,
                memberIntrospectionPolicy);
    }

    public static MethodFinder objectSupport(
            final Class<?> correspondingClass,
            final Can<String> methodNameCandidates,
            final IntrospectionPolicy memberIntrospectionPolicy) {
        return supportMethod(
                correspondingClass,
                methodNameCandidates,
                memberIntrospectionPolicy,
                Domain.Include.class,
                ProgrammingModelConstants.ConflictingAnnotations.OBJECT_SUPPORT);
    }

    public static MethodFinder livecycleCallback(
            final Class<?> correspondingClass,
            final Can<String> methodNameCandidates,
            final IntrospectionPolicy memberIntrospectionPolicy) {
        return supportMethod(
                correspondingClass,
                methodNameCandidates,
                memberIntrospectionPolicy,
                Domain.Include.class,
                ProgrammingModelConstants.ConflictingAnnotations.OBJECT_LIFECYCLE);
    }

    public static MethodFinder memberSupport(
            final Class<?> correspondingClass,
            final Can<String> methodNameCandidates,
            final IntrospectionPolicy memberIntrospectionPolicy) {
        return supportMethod(
                correspondingClass,
                methodNameCandidates,
                memberIntrospectionPolicy,
                Domain.Include.class,
                ProgrammingModelConstants.ConflictingAnnotations.MEMBER_SUPPORT);
    }

    @Getter private final @NonNull Class<?> correspondingClass;
    @Getter private final @NonNull EncapsulationPolicy encapsulationPolicy;
    @Getter private final @NonNull Predicate<ResolvedMethod> mustSatisfy;
    private final @NonNull Can<String> methodNameCandidates;

    public Stream<ResolvedMethod> streamMethodsMatchingSignature(
            final @Nullable Class<?>[] paramTypes) {

        if(paramTypes==null) {
            return streamMethodsIgnoringSignature();
        }

        var type = getCorrespondingClass();
        var classCache = _ClassCache.getInstance();
        var isEncapsulationSupported = getEncapsulationPolicy().isEncapsulatedMembersSupported();

        if(methodNameCandidates.equals(ANY_NAME)) {
            //stream all
            return (isEncapsulationSupported
                    ? classCache.streamResolvedMethods(type)
                    : classCache.streamPublicMethods(type))
                        .filter(method->Arrays.equals(paramTypes, method.paramTypes()))
                        .filter(mustSatisfy);
        }

        return methodNameCandidates.stream()
        .map(name->isEncapsulationSupported
                ? classCache.lookupResolvedMethod(type, name, paramTypes)
                : classCache.lookupPublicMethod(type, name, paramTypes))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(mustSatisfy);
    }

    public Stream<ResolvedMethod> streamMethodsIgnoringSignature() {
        var type = getCorrespondingClass();
        var classCache = _ClassCache.getInstance();
        var isEncapsulationSupported = getEncapsulationPolicy().isEncapsulatedMembersSupported();
        return (isEncapsulationSupported
                ? classCache.streamResolvedMethods(type)
                : classCache.streamPublicMethods(type))
                    .filter(mustSatisfy);
    }

    // -- WITHERS

    public MethodFinder withRequiredReturnType(final @NonNull Class<?> requiredReturnType) {
        return new MethodFinder(
                correspondingClass,
                encapsulationPolicy,
                mustSatisfy.and(resolvedMethod->resolvedMethod.isReturnTypeATypeOf(requiredReturnType)),
                methodNameCandidates);
    }

    public MethodFinder withReturnTypeAnyOf(final @NonNull Can<Class<?>> anyOfReturnTypes) {
        return new MethodFinder(
                correspondingClass,
                encapsulationPolicy,
                mustSatisfy.and(resolvedMethod->resolvedMethod.isReturnTypeAnyTypeOf(anyOfReturnTypes)),
                methodNameCandidates);
    }

    // -- HELPER

    private static MethodFinder havingAnyOrNoAnnotation(
            final Class<?> correspondingClass,
            final Can<String> methodNameCandidates,
            final IntrospectionPolicy memberIntrospectionPolicy) {
        return of(
                correspondingClass,
                methodNameCandidates,
                memberIntrospectionPolicy.getEncapsulationPolicy(),
                _Predicates.alwaysTrue());
    }

    private static MethodFinder supportMethod(
            final Class<?> correspondingClass,
            final Can<String> methodNameCandidates,
            final IntrospectionPolicy memberIntrospectionPolicy,
            final Class<? extends Annotation> annotationType,
            final ConflictingAnnotations conflictingAnnotations) {

        var finder = of(
                correspondingClass,
                methodNameCandidates,
                // support methods are always allowed private
                EncapsulationPolicy.ENCAPSULATED_MEMBERS_SUPPORTED,
                havingAnnotationIfEnforcedByPolicyOrAccessibility(
                        memberIntrospectionPolicy.getSupportMethodAnnotationPolicy().isSupportMethodAnnotationsRequired(),
                        annotationType,
                        conflictingAnnotations.getProhibits()));

        return finder;
    }

    private static Predicate<ResolvedMethod> havingAnnotationIfEnforcedByPolicyOrAccessibility(
            final boolean annotationRequired,
            final Class<? extends Annotation> annotationType,
            final Can<Class<? extends Annotation>> conflictingAnnotations) {

        return annotationRequired
                    ? method->havingAnnotation(method, annotationType, conflictingAnnotations)
                    : method->havingAnnotationOrPublic(method, annotationType, conflictingAnnotations);
    }

    private static boolean havingAnnotationOrPublic(
            final ResolvedMethod method,
            final Class<? extends Annotation> annotationType,
            final Can<Class<? extends Annotation>> conflictingAnnotations) {

        return _Reflect.isPublicNonSynthetic(method.method())
                ? true
                : havingAnnotation(method, annotationType, conflictingAnnotations);
    }

    //FIXME[CAUSEWAY-2774] if annotation appears on an abstract method that was inherited with given method,
    // its not detected here
    private static boolean havingAnnotation(
            final ResolvedMethod method,
            final Class<? extends Annotation> annotationType,
            final Can<Class<? extends Annotation>> conflictingAnnotations) {

        var isMarkerAnnotationPresent = _Annotations.synthesize(method.method(), annotationType).isPresent();
        if(isMarkerAnnotationPresent) {

            var isConflictingAnnotationPresent = conflictingAnnotations
            .stream()
            .anyMatch(conflictingAnnotationType->
                    _Annotations.synthesize(method.method(), conflictingAnnotationType).isPresent());

            // do not pickup this method if conflicting - so meta-model validation will fail later on
            return !isConflictingAnnotationPresent;
        }
        return isMarkerAnnotationPresent;
    }

}
