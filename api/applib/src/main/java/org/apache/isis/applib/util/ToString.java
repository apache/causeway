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

import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.isis.commons.internal.collections._Lists;

/**
 * Fluent Object to String Composition.
 *
 * @param <T>
 * @since 2.0 {@index}
 *
 */
public class ToString<T> {

    public static <T> ToString<T> toString(String name, Function<? super T, ?> getter) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(getter);
        return new ToString<>(name, getter, false);
    }

    public static <T> ToString<T> toStringOmitIfAbsent(String name, Function<? super T, ?> getter) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(getter);
        return new ToString<>(name, getter, true);
    }

    private final List<String> names = _Lists.newArrayList();
    private final List<Function<? super T, ?>> getters = _Lists.newArrayList();
    private final BitSet omitIfAbsent = new BitSet();

    private ToString(String name, Function<? super T, ?> getter, boolean omitIfAbsent) {
        addBit(omitIfAbsent);
        names.add(name);
        getters.add(getter);
    }

    public ToString<T> thenToString(String name, Function<? super T, ?> getter){
        Objects.requireNonNull(name);
        Objects.requireNonNull(getter);
        addBit(false);
        names.add(name);
        getters.add(getter);
        return this;
    }

    public ToString<T> thenToStringOmitIfAbsent(String name, Function<? super T, ?> getter){
        Objects.requireNonNull(name);
        Objects.requireNonNull(getter);
        addBit(true);
        names.add(name);
        getters.add(getter);
        return this;
    }

    public String toString(T target){
        return toString(target, value->""+value);
    }

    public String toString(T target, Function<Object, String> valueToStringFunction){

        if(valueToStringFunction==null) {
            return toString(target);
        }

        if(target==null) {
            return "null";
        }

        Objects.requireNonNull(valueToStringFunction);

        final int[] index = {-1}; // value reference

        return String.format("%s{%s}",

                target.getClass().getSimpleName(),

                getters.stream()
                .peek(__->index[0]++)
                .map(getter->getter.apply(target))
                .filter(value->value!=null || !omitIfAbsent.get(index[0]))
                .map(valueToStringFunction)
                .map(valueLiteral->names.get(index[0])+"="+valueLiteral)
                .collect(Collectors.joining(", "))

                );
    }

    // -- HELPER

    private void addBit(boolean bit) {
        final int index = names.size();
        if(bit) {
            omitIfAbsent.set(index);
        } else {
            omitIfAbsent.clear(index);
        }
    }

}
