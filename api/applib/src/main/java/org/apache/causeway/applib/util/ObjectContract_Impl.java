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
package org.apache.causeway.applib.util;

import java.util.Comparator;
import java.util.function.Function;

import org.apache.causeway.applib.util.ObjectContracts.ObjectContract;

/**
 * Package private default implementation for ObjectContract.
 *
 * @since 2.0
 */
class ObjectContract_Impl<T> implements ObjectContract<T> {

    private final Equality<T> equality;
    private final Hashing<T> hashing;
    private final ToString<T> toString;
    private final Comparator<T> comparator;

    Function<Object, String> valueToStringFunction;

    // -- CONSTRUCTION

    ObjectContract_Impl(
            Equality<T> equality,
            Hashing<T> hashing,
            ToString<T> toString,
            Comparator<T> comparator) {
        this.equality = equality;
        this.hashing = hashing;
        this.toString = toString;
        this.comparator = comparator;
    }

    // -- INTERFACE

    @Override
    public int compare(T obj, T other) {
        return comparator.compare(obj, other);
    }

    @Override
    public boolean equals(T obj, Object other) {
        return equality.equals(obj, other);
    }

    @Override
    public int hashCode(T obj) {
        return hashing.hashCode(obj);
    }

    @Override
    public String toString(T obj) {
        return toString.toString(obj, valueToStringFunction);
    }

    // -- WITHER FOR VALUE TO STRING FUNCTION

    @Override
    public ObjectContract<T> withValueToStringFunction(Function<Object, String> valueToStringFunction) {
        this.valueToStringFunction = valueToStringFunction;
        return this;
    }

    // -- COMPOSITION

    @Override
    public <U> ObjectContract<T> thenUse(String propertyLabel, Function<? super T, ? extends U> getter,
            Comparator<? super U> valueComparator) {

        final ObjectContract_Impl<T> contract =  new ObjectContract_Impl<>(
                this.equality.thenCheckEquals(getter),
                this.hashing.thenHashing(getter),
                this.toString.thenToString(propertyLabel, getter),
                this.comparator.thenComparing(Comparator.comparing(getter, valueComparator))
                );

        contract.valueToStringFunction = this.valueToStringFunction;

        return contract;
    }



}
