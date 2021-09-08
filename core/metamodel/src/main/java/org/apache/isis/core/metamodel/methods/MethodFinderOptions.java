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
package org.apache.isis.core.metamodel.methods;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.annotation.Domain;
import org.apache.isis.applib.annotation.Introspection.EncapsulationPolicy;
import org.apache.isis.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.functions._Predicates;
import org.apache.isis.commons.internal.reflection._Annotations;
import org.apache.isis.commons.internal.reflection._ClassCache;
import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants.ConflictingAnnotations;
import org.apache.isis.core.metamodel.commons.MethodUtil;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MethodFinderOptions {

    public static MethodFinderOptions of(
            final @NonNull Class<?> correspondingClass,
            final @NonNull Can<String> methodNameCandidatesPossiblyDuplicated,
            final @NonNull EncapsulationPolicy encapsulationPolicy,
            final @NonNull Predicate<Method> mustSatisfy) {

        final Predicate<Method> isNotStatic = MethodUtil::isNotStatic;
        val methodNameCandidates = methodNameCandidatesPossiblyDuplicated.distinct();

        return new MethodFinderOptions(
                correspondingClass,
                encapsulationPolicy,
                methodNameCandidates.equals(ANY_NAME)
                        ? isNotStatic.and(mustSatisfy)
                        : isNotStatic
                            .and(method->methodNameCandidates.contains(method.getName()))
                            .and(mustSatisfy),
                methodNameCandidates);
    }

    public static final Can<String> ANY_NAME = Can.of(""); // arbitrary marker
    public static final Class<?>[] NO_ARG = new Class<?>[0];
    //private static final Class<?>[] ANY_ARG = new Class<?>[] {void.class}; // arbitrary marker

    public static MethodFinderOptions notNecessarilyPublic(
            final Class<?> correspondingClass,
            final Can<String> methodNameCandidates) {
        return of(
                correspondingClass,
                methodNameCandidates,
                EncapsulationPolicy.ENCAPSULATED_MEMBERS_SUPPORTED,
                _Predicates.alwaysTrue()
                );
    }

    public static MethodFinderOptions publicOnly(
            final Class<?> correspondingClass,
            final Can<String> methodNameCandidates) {
        return of(
                correspondingClass,
                methodNameCandidates,
                EncapsulationPolicy.ONLY_PUBLIC_MEMBERS_SUPPORTED,
                _Predicates.alwaysTrue()
                );
    }

    public static MethodFinderOptions accessor(
            final Class<?> correspondingClass,
            final Can<String> methodNameCandidates,
            final IntrospectionPolicy memberIntrospectionPolicy) {
        return havingAnyOrNoAnnotation(
                correspondingClass,
                methodNameCandidates,
                memberIntrospectionPolicy);
    }

    public static MethodFinderOptions objectSupport(
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

    public static MethodFinderOptions livecycleCallback(
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

    public static MethodFinderOptions memberSupport(
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
    @Getter private final @NonNull Predicate<Method> mustSatisfy;
    private final @NonNull Can<String> methodNameCandidates;

    public Stream<Method> streamMethodsMatchingSignature(
            final @Nullable Class<?>[] paramTypes) {

        if(paramTypes==null) {
            return streamMethodsIgnoringSignature();
        }

        val type = getCorrespondingClass();
        val classCache = _ClassCache.getInstance();
        val isEncapsulationSupported = getEncapsulationPolicy().isEncapsulatedMembersSupported();

        if(methodNameCandidates.equals(ANY_NAME)) {
            //stream all
            return (isEncapsulationSupported
                    ? classCache.streamPublicOrDeclaredMethods(type)
                    : classCache.streamPublicMethods(type))
                        .filter(method->Arrays.equals(paramTypes, method.getParameterTypes()))
                        .filter(mustSatisfy);
        }

        return methodNameCandidates.stream()
        .map(name->isEncapsulationSupported
                ? classCache.lookupPublicOrDeclaredMethod(type, name, paramTypes)
                : classCache.lookupPublicMethod(type, name, paramTypes))
        .filter(_NullSafe::isPresent)
        .filter(mustSatisfy);

    }

    public Stream<Method> streamMethodsIgnoringSignature() {
        val type = getCorrespondingClass();
        val classCache = _ClassCache.getInstance();
        val isEncapsulationSupported = getEncapsulationPolicy().isEncapsulatedMembersSupported();
        return (isEncapsulationSupported
                ? classCache.streamPublicOrDeclaredMethods(type)
                : classCache.streamPublicMethods(type))
                    .filter(mustSatisfy);
    }

    // -- WITHERS

    public MethodFinderOptions withRequiredReturnType(final @NonNull Class<?> requiredReturnType) {
        return new MethodFinderOptions(
                correspondingClass,
                encapsulationPolicy,
                mustSatisfy.and(MethodFinder.hasReturnType(requiredReturnType)),
                methodNameCandidates);
    }

    public MethodFinderOptions withReturnTypeAnyOf(final @NonNull Can<Class<?>> anyOfReturnTypes) {
        return new MethodFinderOptions(
                correspondingClass,
                encapsulationPolicy,
                mustSatisfy.and(MethodFinder.hasReturnTypeAnyOf(anyOfReturnTypes)),
                methodNameCandidates);
    }


    // -- HELPER

    private static MethodFinderOptions havingAnyOrNoAnnotation(
            final Class<?> correspondingClass,
            final Can<String> methodNameCandidates,
            final IntrospectionPolicy memberIntrospectionPolicy) {
        return of(
                correspondingClass,
                methodNameCandidates,
                memberIntrospectionPolicy.getEncapsulationPolicy(),
                _Predicates.alwaysTrue());
    }

    private static MethodFinderOptions supportMethod(
            final Class<?> correspondingClass,
            final Can<String> methodNameCandidates,
            final IntrospectionPolicy memberIntrospectionPolicy,
            final Class<? extends Annotation> annotationType,
            final ConflictingAnnotations conflictingAnnotations) {

        return of(
                correspondingClass,
                methodNameCandidates,
                // support methods are always allowed private
                EncapsulationPolicy.ENCAPSULATED_MEMBERS_SUPPORTED,
                havingAnnotationIfEnforcedByPolicyOrAccessibility(
                        memberIntrospectionPolicy,
                        annotationType,
                        conflictingAnnotations.getProhibits()));

    }

    private static Predicate<Method> havingAnnotationIfEnforcedByPolicyOrAccessibility(
            final IntrospectionPolicy memberIntrospectionPolicy,
            final Class<? extends Annotation> annotationType,
            final Can<Class<? extends Annotation>> conflictingAnnotations) {

        //MemberAnnotationPolicy
        //  when REQUIRED -> annot. on support also required
        //  when OPTIONAL -> annot. on support only required when support method is private

        return memberIntrospectionPolicy.getMemberAnnotationPolicy().isMemberAnnotationsRequired()
                    ? method->havingAnnotation(method, annotationType, conflictingAnnotations)
                    : method-> !_Reflect.isAccessible(method)
                            ? havingAnnotation(method, annotationType, conflictingAnnotations)
                            : true;

    }

    //FIXME[ISIS-2774] if annotation appears on an abstract method that was inherited with given method,
    // its not detected here
    private static boolean havingAnnotation(
            final Method method,
            final Class<? extends Annotation> annotationType,
            final Can<Class<? extends Annotation>> conflictingAnnotations) {

        val isMarkerAnnotationPresent = _Annotations.synthesizeInherited(method, annotationType).isPresent();
        if(isMarkerAnnotationPresent) {

            val isConflictingAnnotationPresent = conflictingAnnotations
            .stream()
            .anyMatch(conflictingAnnotationType->
                    _Annotations.synthesizeInherited(method, conflictingAnnotationType).isPresent());

            // do not pickup this method if conflicting - so meta-model validation will fail later on
            return !isConflictingAnnotationPresent;
        }
        return isMarkerAnnotationPresent;
    }






}
