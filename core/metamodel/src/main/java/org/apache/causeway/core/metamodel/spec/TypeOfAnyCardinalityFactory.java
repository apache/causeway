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

import org.springframework.core.ResolvableType;

import org.apache.causeway.commons.collectionsemantics.CollectionSemantics;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._GenericResolver.TypeOfAnyCardinality;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.commons.internal.reflection._Reflect.ConstructorAndImplementingClass;
import org.apache.causeway.commons.internal.reflection._Reflect.MethodAndImplementingClass;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TypeOfAnyCardinalityFactory {

    public TypeOfAnyCardinality forMethodFacadeReturn(
            final Class<?> _implementationClass, final MethodFacade methodFacade) {
        return forMethodReturn(_implementationClass, methodFacade.asMethodForIntrospection());
    }

    public TypeOfAnyCardinality forMethodReturn(
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
                TypeOfAnyCardinality.plural(
                        adopted.resolveFirstGenericTypeArgumentOnMethodReturn(),
                        methodReturn,
                        collectionSemantics)
            )
            .orElseGet(()->TypeOfAnyCardinality.singular(methodReturn));
        })
        .orElseGet(()->TypeOfAnyCardinality.singular(methodReturnGuess));
    }

    public TypeOfAnyCardinality forMethodFacadeParameter(
            final Class<?> _implementationClass, final MethodFacade methodFacade, final int paramIndex) {
        val executable = methodFacade.asExecutable();
        if(executable instanceof Method) {
            return forMethodParameter(_implementationClass, (Method)executable, paramIndex);
        } else if(executable instanceof Constructor) {
            return forConstructorParameter(_implementationClass, (Constructor<?>)executable, paramIndex);
        }
        throw _Exceptions.unexpectedCodeReach();
    }

    public TypeOfAnyCardinality forConstructorParameter(
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
                TypeOfAnyCardinality.plural(
                        adopted.resolveFirstGenericTypeArgumentOnParameter(paramIndex),
                        paramType,
                        collectionSemantics)
            )
            .orElseGet(()->TypeOfAnyCardinality.singular(paramType));
        })
        .orElseGet(()->TypeOfAnyCardinality.singular(paramTypeGuess));
    }

    public TypeOfAnyCardinality forMethodParameter(
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
                TypeOfAnyCardinality.plural(
                        adopted.resolveFirstGenericTypeArgumentOnParameter(paramIndex),
                        paramType,
                        collectionSemantics)
            )
            .orElseGet(()->TypeOfAnyCardinality.singular(paramType));
        })
        .orElseGet(()->TypeOfAnyCardinality.singular(paramTypeGuess));
    }

    public TypeOfAnyCardinality forPluralType(
            final @NonNull Class<?> pluralType,
            final @NonNull CollectionSemantics collectionSemantics) {
        return TypeOfAnyCardinality.plural(
                toClass(ResolvableType.forClass(pluralType)),
                pluralType,
                collectionSemantics);
    }

    // -- HELPER

    private Class<?> toClass(final ResolvableType pluralType){
        val genericTypeArg = pluralType.isArray()
                ? pluralType.getComponentType()
                : pluralType.getGeneric(0);
        return genericTypeArg.toClass();
    }

}
