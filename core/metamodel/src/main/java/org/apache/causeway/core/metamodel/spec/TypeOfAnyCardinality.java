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
package org.apache.causeway.core.metamodel.spec;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

import org.springframework.core.ResolvableType;

import org.apache.causeway.commons.collectionsemantics.CollectionSemantics;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._GenericResolver;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.commons.internal.reflection._Reflect.ConstructorAndImplementingClass;
import org.apache.causeway.commons.internal.reflection._Reflect.MethodAndImplementingClass;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.Accessors;

@lombok.Value(staticConstructor = "of") @Accessors(fluent=true)
public class TypeOfAnyCardinality implements _GenericResolver.TypeOfAnyCardinality {

    @Getter(onMethod_={@Override})
    private final @NonNull Class<?> elementType;
    @Getter(onMethod_={@Override})
    private final @NonNull Optional<Class<?>> containerType;
    @Getter(onMethod_={@Override})
    private final @NonNull Optional<CollectionSemantics> collectionSemantics;

    /**
     * Always <code>true</code> for <i>scalar</i> or <i>array</i>.
     * Otherwise, whether {@link #containerType()} exactly matches
     * the container type from {@link #collectionSemantics()}.
     */
    public boolean isSupportedForActionParameter() {
        return isSingular()
                || isArray()
                ? true
                : Objects.equals(
                        containerType().orElse(null),
                        collectionSemantics().map(CollectionSemantics::getContainerType).orElse(null));

    }

    // -- FACTORIES

    public static TypeOfAnyCardinality singular(final @NonNull Class<?> singularType) {
        return of(assertSingular(singularType), Optional.empty(), Optional.empty());
    }

    public static TypeOfAnyCardinality plural(
            final @NonNull Class<?> elementType,
            final @NonNull Class<?> pluralType,
            final @NonNull CollectionSemantics collectionSemantics) {
        if(CollectionSemantics.valueOf(elementType).isPresent()) {
            System.err.printf("nested plural detected %s: yields unpredicted behavior%n", elementType);
        }
        return of(assertSingular(elementType),
                Optional.of(assertPlural(pluralType)),
                Optional.of(collectionSemantics));
    }

    public static TypeOfAnyCardinality forMethodFacadeReturn(
            final Class<?> _implementationClass, final MethodFacade methodFacade) {
        return forMethodReturn(_implementationClass, methodFacade.asMethodForIntrospection());
    }

    public static TypeOfAnyCardinality forMethodReturn(
            final Class<?> _implementationClass, final ResolvedMethod _method) {
        val methodReturnGuess = _method.returnType();
        return CollectionSemantics.valueOf(methodReturnGuess)
        .map(__->{
            // adopt into default class loader context

            val origin = MethodAndImplementingClass.of(_method.method(), _implementationClass);
            val adopted = origin
                    .adoptIntoDefaultClassLoader()
                    .getValue()
                    .orElse(origin);

            val method = adopted.getMethod();
            val methodReturn = method.getReturnType();

            return CollectionSemantics.valueOf(methodReturn)
            .map(collectionSemantics->
                plural(
                        adopted.resolveFirstGenericTypeArgumentOnMethodReturn(),
                        methodReturn,
                        collectionSemantics)
            )
            .orElseGet(()->singular(methodReturn));
        })
        .orElseGet(()->singular(methodReturnGuess));
    }

    public static TypeOfAnyCardinality forMethodFacadeParameter(
            final Class<?> _implementationClass, final MethodFacade methodFacade, final int paramIndex) {
        val executable = methodFacade.asExecutable();
        if(executable instanceof Method) {
            return forMethodParameter(_implementationClass, (Method)executable, paramIndex);
        } else if(executable instanceof Constructor) {
            return forConstructorParameter(_implementationClass, (Constructor<?>)executable, paramIndex);
        }
        throw _Exceptions.unexpectedCodeReach();
    }

    public static TypeOfAnyCardinality forConstructorParameter(
            final Class<?> _implementationClass, final Constructor<?> _constructor, final int paramIndex) {
        val paramTypeGuess = _constructor.getParameters()[paramIndex].getType();
        return CollectionSemantics.valueOf(paramTypeGuess)
        .map(__->{
            // adopt into default class loader context

            val origin = ConstructorAndImplementingClass.of(_constructor, _implementationClass);
            val adopted = origin
                    .adoptIntoDefaultClassLoader()
                    .getValue()
                    .orElse(origin);

            val constructor = adopted.getConstructor();
            val paramType = constructor.getParameters()[paramIndex].getType();

            return CollectionSemantics.valueOf(paramType)
            .map(collectionSemantics->
                plural(
                        adopted.resolveFirstGenericTypeArgumentOnParameter(paramIndex),
                        paramType,
                        collectionSemantics)
            )
            .orElseGet(()->singular(paramType));
        })
        .orElseGet(()->singular(paramTypeGuess));
    }

    public static TypeOfAnyCardinality forMethodParameter(
            final Class<?> _implementationClass, final Method _method, final int paramIndex) {
        val paramTypeGuess = _method.getParameters()[paramIndex].getType();
        return CollectionSemantics.valueOf(paramTypeGuess)
        .map(__->{
            // adopt into default class loader context

            val origin = MethodAndImplementingClass.of(_method, _implementationClass);
            val adopted = origin
                    .adoptIntoDefaultClassLoader()
                    .getValue()
                    .orElse(origin);

            val method = adopted.getMethod();
            val paramType = method.getParameters()[paramIndex].getType();

            return CollectionSemantics.valueOf(paramType)
            .map(collectionSemantics->
                plural(
                        adopted.resolveFirstGenericTypeArgumentOnParameter(paramIndex),
                        paramType,
                        collectionSemantics)
            )
            .orElseGet(()->singular(paramType));
        })
        .orElseGet(()->singular(paramTypeGuess));
    }

    public static TypeOfAnyCardinality forPluralType(
            final @NonNull Class<?> nonScalarType,
            final @NonNull CollectionSemantics collectionSemantics) {
        return plural(
                toClass(ResolvableType.forClass(nonScalarType)),
                nonScalarType,
                collectionSemantics);
    }

    // -- WITHERS

    public TypeOfAnyCardinality withElementType(final @NonNull Class<?> elementType) {
        return of(assertSingular(elementType), this.containerType(), this.collectionSemantics());
    }

    // -- HELPER

    private static Class<?> assertSingular(final @NonNull Class<?> singularType) {
        _Assert.assertEquals(
                Optional.empty(),
                CollectionSemantics.valueOf(singularType),
                ()->String.format("%s should not match any supported plural (collection) types", singularType));
        return singularType;
    }

    private static Class<?> assertPlural(final @NonNull Class<?> pluralType) {
        _Assert.assertTrue(
                CollectionSemantics.valueOf(pluralType).isPresent(),
                ()->String.format("%s should match a supported plural (collection) type", pluralType));
        return pluralType;
    }

    private static Class<?> toClass(final ResolvableType pluralType){
        val genericTypeArg = pluralType.isArray()
                ? pluralType.getComponentType()
                : pluralType.getGeneric(0);
        return genericTypeArg.toClass();
    }

}
