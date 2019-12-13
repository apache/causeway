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

package org.apache.isis.applib.util;

import java.util.Comparator;
import java.util.function.Function;

import org.apache.isis.applib.util.ObjectContracts.ObjectContract;

/**
 * Package private default implementation for an empty ObjectContract.
 *
 * @since 2.0
 */
class ObjectContract_Empty<T> implements ObjectContract<T> {

    private final static String UNDEFINED_CONTRACT = "object's contract is not defined (empty)";

    private Function<Object, String> valueToStringFunction;

    public ObjectContract_Empty(Class<T> objectClass) {

    }

    @Override
    public int compare(T obj, T other) {
        throw undefined();
    }

    @Override
    public boolean equals(T obj, Object other) {
        throw undefined();
    }

    @Override
    public int hashCode(T obj) {
        throw undefined();
    }

    @Override
    public String toString(T obj) {
        return UNDEFINED_CONTRACT;
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

        final ObjectContract_Impl<T> contract = new ObjectContract_Impl<T>(
                Equality.checkEquals(getter),
                Hashing.hashing(getter),
                ToString.toString(propertyLabel, getter),
                Comparator.comparing(getter, valueComparator));

        contract.valueToStringFunction = this.valueToStringFunction;

        return contract;
    }

    // -- HELPER

    private final static IllegalArgumentException undefined() {
        return new IllegalArgumentException(UNDEFINED_CONTRACT);
    }


}
