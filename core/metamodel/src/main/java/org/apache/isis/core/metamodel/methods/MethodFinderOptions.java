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
import java.util.function.Predicate;

import org.apache.isis.applib.annotation.Introspection.EncapsulationPolicy;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.commons.internal.functions._Predicates;
import org.apache.isis.commons.internal.reflection._Annotations;
import org.apache.isis.core.metamodel.facets.MemberIntrospectionPolicy;

import lombok.Value;

@Value(staticConstructor = "of")
public class MethodFinderOptions {

    public static MethodFinderOptions notNecessarilyPublic() {
        return of(
                EncapsulationPolicy.ENCAPSULATED_MEMBERS_SUPPORTED,
                _Predicates.alwaysTrue()
                );
    }

    public static MethodFinderOptions publicOnly() {
        return of(
                EncapsulationPolicy.ONLY_PUBLIC_MEMBERS_SUPPORTED,
                _Predicates.alwaysTrue()
                );
    }

    public static MethodFinderOptions accessor(
            final MemberIntrospectionPolicy memberIntrospectionPolicy) {
        return havingAnyOrNoAnnotation(memberIntrospectionPolicy);
    }

    public static MethodFinderOptions memberSupport(
            final MemberIntrospectionPolicy memberIntrospectionPolicy) {
        return havingAnnotation(memberIntrospectionPolicy, MemberSupport.class);
    }

    public static MethodFinderOptions objectSupport(
            final MemberIntrospectionPolicy memberIntrospectionPolicy) {
        return havingAnyOrNoAnnotation(memberIntrospectionPolicy);
    }

    public static MethodFinderOptions livecycleCallback(
            final MemberIntrospectionPolicy memberIntrospectionPolicy) {
        return havingAnyOrNoAnnotation(memberIntrospectionPolicy);
    }

    public static MethodFinderOptions layoutSupport(
            final MemberIntrospectionPolicy memberIntrospectionPolicy) {
        return havingAnyOrNoAnnotation(memberIntrospectionPolicy);
    }

    private final EncapsulationPolicy encapsulationPolicy;
    private final Predicate<Method> mustSatisfy;

    // -- HELPER

    private static MethodFinderOptions havingAnyOrNoAnnotation(
            final MemberIntrospectionPolicy memberIntrospectionPolicy) {
        return of(
                memberIntrospectionPolicy.getEncapsulationPolicy(),
                _Predicates.alwaysTrue());
    }


    private static MethodFinderOptions havingAnnotation(
            final MemberIntrospectionPolicy memberIntrospectionPolicy,
            final Class<? extends Annotation> associatedAnnotationType) {
        return of(
                memberIntrospectionPolicy.getEncapsulationPolicy(),
                memberIntrospectionPolicy.getMemberAnnotationPolicy().isMemberAnnotationsRequired()
                    ? method->_Annotations.synthesize(method, associatedAnnotationType).isPresent()
                    : _Predicates.alwaysTrue());
    }

}
