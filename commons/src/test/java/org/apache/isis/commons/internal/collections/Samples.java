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
package org.apache.isis.commons.internal.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class Samples {

    static Iterable<Integer> iterable(final Integer ... elements) {
        return new Iterable<Integer>() {
        
            @Override
            public Iterator<Integer> iterator() {
                return Arrays.asList(elements).iterator();
            }
        };
    }

    /**
     * order matters
     */
    void assertListEquals(List<Integer> expected, Collection<Integer> actual) {
        assertEquals(expected, new ArrayList<>(actual));
    }

    /**
     * order is ignored
     */
    void assertSetEquals(Set<Integer> expected, Collection<Integer> actual) {
        assertEquals(
                new HashSet<Integer>(expected).removeAll(actual),
                new HashSet<Integer>(actual).removeAll(expected));
    }
    
    
    @FunctionalInterface
    interface IndexedBiConsumer<T, U> {
        void accept(int i, int j, T t, U u);
    }
    
    static class FiniteSpace2 {
        
        static void visit(
                List<Integer[]> as, 
                List<Integer[]> bs, 
                IndexedBiConsumer<Integer[], Integer[]> probe) {
            int i=0;
            for(val a : as) {
                int j=0;
                for(val b : bs) {
                    probe.accept(i, j++, a, b);
                }
                i++;
            }
        }
    }
    
}
