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
package org.apache.isis.commons.collections;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName="of")
final class Can_Singleton<T> implements Can<T> {

    private final T element;

    @Getter(lazy=true, onMethod=@__({@Override})) 
    private final Optional<T> singleton = Optional.of(element);

    @Override
    public Cardinality getCardinality() {
        return Cardinality.ONE;
    }

    @Override
    public Stream<T> stream() {
        return Stream.of(element);
    }

    @Override
    public Optional<T> getFirst() {
        return getSingleton();
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.singletonList(element).iterator();
    }

    @Override
    public String toString() {
        return "Bin["+element+"]";
    }

}
