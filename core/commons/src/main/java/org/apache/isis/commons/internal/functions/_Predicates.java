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
package org.apache.isis.commons.internal.functions;

import java.util.Objects;
import java.util.function.Predicate;

import org.apache.isis.commons.internal.base._NullSafe;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * General purpose Predicates.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public final class _Predicates {

    /**
     * @param operand
     * @return a Predicate that tests its argument against equality to the given {@code operand}
     */
    public static <T> Predicate<T> equalTo(T operand) {
        return arg->Objects.equals(arg, operand);
    }

    /**
     * @param operand
     * @return a Predicate that tests its argument whether it is the same instance as the given {@code operand}
     */
    public static <T> Predicate<T> sameAs(T operand) {
        return arg-> arg == operand;
    }

    /**
     * 
     * @return a Predicate that always tests true
     */
    public static <T> Predicate<T> alwaysTrue() {
        return __->true;
    }

    /**
     * @return a Predicate that tests for the operand to be not null
     */
    public static <T> Predicate<T> isPresent() {
        return _NullSafe::isPresent;
    }

    /**
     * Negates the specified {@code predicate}. (Obsolete with Java-11) 
     * @param predicate
     */
    public static <T> Predicate<T> not(Predicate<T> predicate) {
        return null;
    }

    /**
     * @param superClass
     * @return a Predicate that tests for the operand to be an instance of {@code superClass}
     */
    public static Predicate<Object> instanceOf(Class<?> superClass) {
        return obj->superClass.isAssignableFrom(obj.getClass());
    }

}
