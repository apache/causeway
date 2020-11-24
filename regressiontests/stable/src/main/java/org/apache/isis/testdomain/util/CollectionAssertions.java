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
package org.apache.isis.testdomain.util;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.val;

public final class CollectionAssertions {

    public static void assertComponentWiseEquals(Object a, Object b) {
        
        val array1 = _NullSafe.streamAutodetect(a)
            .collect(_Arrays.toArray(Object.class));
        val array2 = _NullSafe.streamAutodetect(b)
            .collect(_Arrays.toArray(Object.class));
        
        assertArrayEquals(array1, array2);
        
    }
    
    public static <T> void assertComponentWiseEquals(Object a, Object b, BiFunction<T, T, String> difference) {
        
        @SuppressWarnings("unchecked")
        final List<T> list1 = _NullSafe.streamAutodetect(a)
                .map(t->(T)t)
                .collect(Collectors.toList());
        @SuppressWarnings("unchecked")
        final List<T> list2 = _NullSafe.streamAutodetect(b)
                .map(t->(T)t)
                .collect(Collectors.toList());
        
        assertEquals(list1.size(), list2.size(), "expected element count equals");
        
        for(int i=0; i<list1.size(); ++i) {
            
            final int index = i;
            val u = list1.get(i);
            val v = list2.get(i);
            
            val delta = difference.apply(u, v);
            
            if(delta!=null) {
                Assertions.fail(String.format("elements at index %d differ: %s", index, delta));
            }
            
        }
        
    }
    
    public static void assertComponentWiseNumberEquals(Object a, Object b) {
        
        val array1 = _NullSafe.streamAutodetect(a)
            .map(Number.class::cast)
            .collect(_Arrays.toArray(Number.class));
        val array2 = _NullSafe.streamAutodetect(b)
            .map(Number.class::cast)
            .collect(_Arrays.toArray(Number.class));
        
        assertEquals(array1.length, array2.length);
        
        for(int i=0; i<array1.length; ++i) {
            assertNumberEqualsPoorManEdition(array1[i], array2[i]);
        }
        
    }
    
    public static void assertComponentWiseUnwrappedEquals(Object a, Object b) {
        
        val array1 = _NullSafe.streamAutodetect(a)
            .map(element->(element instanceof ManagedObject) 
                    ? ((ManagedObject)element).getPojo()
                    : element)
            .collect(_Arrays.toArray(Object.class));
        
        val array2 = _NullSafe.streamAutodetect(b)
                .map(element->(element instanceof ManagedObject) 
                        ? ((ManagedObject)element).getPojo()
                        : element)
                .collect(_Arrays.toArray(Object.class));
        
        assertArrayEquals(array1, array2);
        
    }

    private static void assertNumberEqualsPoorManEdition(Number a, Number b) {
        if(a==null) {
            assertEquals((Object)null, (Object)b);
            return;
        }
        assertEquals(a.doubleValue(), b.doubleValue(), 1E-9);
    }
    
    
}
