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
package org.apache.causeway.commons.collections;

import java.util.ArrayList;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;

/**
 * As the {@link Can} interface can only provide public methods,
 * we encapsulate optimized unsafe Can constructors here with
 * package private access only.
 */
@UtilityClass
class _CanFactory {

    /**
     * @param <T> element type
     * @param nonNullElements - not containing <code>null</code>
     * @implNote no non-null checks for optimization (package internal use only)
     */
    <T> Can<T> ofNonNullElements(final ArrayList<T> nonNullElements) {

        if(nonNullElements.isEmpty()) {
            return Can.empty();
        }

        if(nonNullElements.size()==1) {
            return Can.ofSingleton(nonNullElements.get(0));
        }

        // let's be heap friendly, ignore below a certain threshold
        if(nonNullElements.size()>32) {
            nonNullElements.trimToSize();
        }

        return Can_Multiple.of(nonNullElements);
    }

    <T> ArrayList<T> arrayListWithSizeUpperBound(final int maxSize) {
        // this is just an optimization, to pre-allocate a reasonable list size,
        // specifically targeted at small list sizes
        return new ArrayList<T>(Math.min(maxSize, 1024));
    }

    <T> Collector<T, ?, ArrayList<T>> toListWithSizeUpperBound(final int maxSize) {
        return Collectors.toCollection(()->
                _CanFactory.arrayListWithSizeUpperBound(maxSize));
    }

}
