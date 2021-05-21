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
package org.apache.isis.core.metamodel.specloader.typeextract;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.reflection._Generics;

import lombok.val;
import lombok.experimental.UtilityClass;

/**
 *
 * @since 2.0
 *
 */
@UtilityClass
public class TypeExtractor {

    /**
     * Helper that finds all parameter types (including generic types) for the
     * provided {@link Method}.
     *
     * <p>
     * For example,
     *
     * <pre>
     * public class CustomerRepository {
     *     public void filterCustomers(List&lt;Customer&gt; customerList) { ... }
     * }
     * </pre>
     * <p>
     * will find both <tt>List</tt> and <tt>Customer</tt>.
     */
    public static Stream<Class<?>> streamMethodParameters(final Method ...methods) {

        val set = _Sets.<Class<?>>newHashSet();

        for(val method : methods) {
            if(method==null) {
                continue;
            }
            acceptNonVoid(set::add, method.getParameterTypes());
            _Generics.streamGenericTypeArgumentsOfMethodParameterTypes(method)
                .forEach(set::add);
        }

        return set.stream();
    }

    /**
     * Helper that finds all return types (including generic types) for the provided
     * {@link Method}.
     *
     * <p>
     * For example,
     *
     * <pre>
     * public class CustomerRepository {
     *     public List&lt;Customer&gt; findCustomers( ... ) { ... }
     * }
     * </pre>
     * <p>
     * will find both <tt>List</tt> and <tt>Customer</tt>.
     */
    public static Stream<Class<?>> streamMethodReturn(final Method ...methods) {

        val set = _Sets.<Class<?>>newHashSet();

        for(val method : methods) {
            if(method==null) {
                continue;
            }
            acceptNonVoid(set::add, method.getReturnType());
            _Generics.streamGenericTypeArgumentsOfMethodReturnType(method)
                .forEach(set::add);
        }

        return set.stream();
    }

    // -- VARIANTS

    public static Stream<Class<?>> streamMethodReturn(final Iterable<Method> methods) {
        val set = _Sets.<Class<?>>newHashSet();

        for(val method : methods) {
            if(method==null) {
                continue;
            }
            acceptNonVoid(set::add, method.getReturnType());
            _Generics.streamGenericTypeArgumentsOfMethodReturnType(method)
                .forEach(set::add);
        }

        return set.stream();
    }

    // -- HELPER

    private static void acceptNonVoid(
            final Consumer<Class<?>> onClass,
            final Class<?>... classes) {

        for (val cls : classes) {
            if(cls != void.class
                    && cls != Void.class) {
                onClass.accept(cls);
            }
        }
    }

}
