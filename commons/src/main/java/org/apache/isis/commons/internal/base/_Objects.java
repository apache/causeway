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
package org.apache.isis.commons.internal.base;

import java.util.Comparator;
import java.util.Objects;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.exceptions._Exceptions;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Provides common object related algorithms.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public final class _Objects {

    /**
     * Compares two objects (nulls-first) in natural order.
     * <p>
     * @apiNote consider using {@link Comparator#naturalOrder()} combined with 
     * {@link Comparator#nullsFirst(Comparator)}. 
     * @implNote this utility method does not produce objects on the heap   
     * @param a
     * @param b
     * @return {@code <0} if {@code a < b}, {@code >0} if {@code a > b} else {@code 0}
     * @see {@link Comparable#compareTo(Object)}
     * @throws UnsupportedOperationException if neither object is comparable
     */
    public static <T> int compareNullsFirst(final @Nullable T a, final @Nullable T b) {
        if(Objects.equals(a, b)) {
            return 0;
        }
        // at this point not both can be null, so which ever is null wins 
        if(a==null) {
            return -1;
        }
        if(b==null) {
            return 1;
        }
        // at this point neither can be null
        if (a instanceof Comparable<?>) {
            return _Casts.<Comparable<T>>uncheckedCast(a).compareTo(b);
        }
        if (b instanceof Comparable<?>) {
            return negate(_Casts.<Comparable<T>>uncheckedCast(b).compareTo(a));
        }
        throw _Exceptions.unsupportedOperation("cannot compare objects if non of them is 'comparable'");
    }
    
    /**
     * Compares two objects (nulls-last) in natural order.
     * <p>
     * @apiNote consider using {@link Comparator#naturalOrder()} combined with 
     * {@link Comparator#nullsLast(Comparator)}. 
     * @implNote this utility method does not produce objects on the heap   
     * @param a
     * @param b
     * @return {@code <0} if {@code a < b}, {@code >0} if {@code a > b} else {@code 0}
     * @see {@link Comparable#compareTo(Object)}
     * @throws UnsupportedOperationException if neither object is comparable 
     */
    public static <T> int compareNullsLast(final @Nullable T a, final @Nullable T b) {
        if(Objects.equals(a, b)) {
            return 0;
        }
        // at this point not both can be null, so which ever is null wins 
        if(a==null) {
            return -1;
        }
        if(b==null) {
            return 1;
        }
        // at this point neither can be null
        if (a instanceof Comparable<?>) {
            return _Casts.<Comparable<T>>uncheckedCast(a).compareTo(b);
        }
        if (b instanceof Comparable<?>) {
            return negate(_Casts.<Comparable<T>>uncheckedCast(b).compareTo(a));
        }
        throw _Exceptions.unsupportedOperation("cannot compare objects if non of them is 'comparable'");
    }

    /**
     * Compares two objects in natural order, both assumed to be non-null.
     * 
     * @apiNote consider using {@link Comparator#naturalOrder()}. 
     * @implNote this utility method does not produce objects on the heap
     * @implNote for performance reasons we don't check for non-null arguments on method entry   
     * 
     * @param a - nun-null
     * @param b - nun-null
     * @return {@code <0} if {@code a < b}, {@code >0} if {@code a > b} else {@code 0}
     * @see {@link Comparable#compareTo(Object)}
     * @throws UnsupportedOperationException if neither object is comparable or both are {@code null} 
     */
    public static <T> int compareNonNull(final /*@NonNull*/ T a, final /*@NonNull*/ T b) {
        if (a instanceof Comparable<?>) {
            return _Casts.<Comparable<T>>uncheckedCast(a).compareTo(b);
        }
        if (b instanceof Comparable<?>) {
            return negate(_Casts.<Comparable<T>>uncheckedCast(b).compareTo(a));
        }
        throw _Exceptions.unsupportedOperation("cannot compare objects if non of them is 'comparable'");
    }
    
    // -- HELPER
    
    private final static int negate(int x) {
        // guard against integer overflow
        if(x==Integer.MIN_VALUE) {
            return 1; 
        }
        /*sonar-ignore-on*/
        return -x;
        /*sonar-ignore-off*/
    }
    
}
