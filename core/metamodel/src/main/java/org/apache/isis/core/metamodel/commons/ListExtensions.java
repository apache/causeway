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
package org.apache.isis.core.metamodel.commons;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

public final class ListExtensions {
    private static final String DEFAULT_DELIMITER = ",";

    private ListExtensions() {
    }

    public static <T> List<T> combineWith(final List<T> extendee, final List<T> list2) {
        final List<T> combinedList = _Lists.newArrayList();
        combinedList.addAll(extendee);
        combinedList.addAll(list2);
        return combinedList;
    }

    /**
     * Returns list1 with everything in list2, ignoring duplicates.
     */
    public static <T> List<T> mergeWith(final List<T> extendee, final List<T> list2) {
        for (final T obj : list2) {
            if (!(extendee.contains(obj))) {
                extendee.add(obj);
            }
        }
        return extendee;
    }



    /**
     * @see #appendDelimitedStringToList(String, String, List)
     */
    public static List<String> appendDelimitedStringToList(final String commaSeparated, final List<String> list) {
        return appendDelimitedStringToList(commaSeparated, DEFAULT_DELIMITER, list);
    }

    public static List<String> appendDelimitedStringToList(final String delimited, final String delimiter, final List<String> list) {
        if (delimited == null) {
            return list;
        }
        final String[] optionValues = delimited.split(delimiter);
        list.addAll(Arrays.asList(optionValues));
        return list;
    }

    // //////////////////////////////////////

    public static <T> List<T> mutableCopy(final List<T> input) {
        return stream(input)
                .collect(Collectors.toList());
    }

    public static <T> List<T> mutableCopy(T[] arr) {
        return stream(arr)
                .collect(Collectors.toList());
    }

    public static <T> void insert(final List<T> list, final int insertionPoint, final T elementToInsert) {
        extend(list, insertionPoint);
        list.add(insertionPoint, elementToInsert);
    }

    public static <T> void adjust(final List<T> list, final int requiredLength) {
        extend(list, requiredLength);
        if(list.size() > requiredLength) {
            list.subList(requiredLength, list.size()).clear();
        }
    }

    private static <T> void extend(final List<T> list, final int requiredLength) {
        for(int i=list.size(); i<requiredLength; i++) {
            list.add(null);
        }
    }

    public static <T> Collection<T> filtered(final List<Object> extendee, final Class<T> type) {
        return _NullSafe.stream(extendee)
                .filter(ClassPredicates.isOfType(type))
                .map(ClassFunctions.castTo(type))
                .collect(Collectors.toList());
    }




}
