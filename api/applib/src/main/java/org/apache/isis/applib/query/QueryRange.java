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
package org.apache.isis.applib.query;

import java.io.Serializable;

/**
 * @since 2.0 {@index}
 */
public interface QueryRange extends Serializable {

    // -- INTERFACE

    boolean isUnconstrained();
    boolean hasOffset();
    boolean hasLimit();

    /**
     * The start index into the set table.
     * (non-negative)
     */
    long getStart();
    
    /**
     * The maximum number of items to return, starting at {@link #getStart()}
     * (non-negative)
     */
    long getLimit();

    /**
     * The end index into the set table. Overflow is ignored.
     * (non-negative)
     */
    long getEnd();
    
    // -- TO INT
    
    /**
     * The start index into the set table (as java int primitive)
     * @throws ArithmeticException - if {@code start} overflows an int
     */
    default int getStartAsInt() {
        return Math.toIntExact(getStart());
    }
    
    /**
     * The maximum number of items to return (as java int primitive)
     * if {@code limit} overflows an int, {@link Integer#MAX_VALUE} is returned.
     */
    default int getLimitAsInt() {
        final long limit = getLimit();
        return limit<=((long)Integer.MAX_VALUE)
                ? Math.toIntExact(limit)
                : Integer.MAX_VALUE;
    }

    /**
     * The end index into the set table. Overflow is ignored. (as java int primitive)
     * if {@code end} overflows an int, {@link Integer#MAX_VALUE} is returned.
     */
    default int getEndAsInt() {
        final long end = getEnd();
        return end<=((long)Integer.MAX_VALUE)
                ? Math.toIntExact(end)
                : Integer.MAX_VALUE;
    }
    
    // -- FACTORIES
    
    public static QueryRange unconstrained() {
        return of(0L, 0L);
    }
    
    public static QueryRange start(long start) {
        return of(start, 0L);
    }
    
    public static QueryRange limit(long limit) {
        return of(0L, limit);
    }
    
    public static QueryRange of(long... range) {
        return new _QueryRangeDefault(range);
    }
    
    // -- WITHERS
    
    public default QueryRange withStart(long start) {
        return of(start, getLimit());
    }
    
    public default QueryRange withLimit(long limit) {
        return of(getStart(), limit);
    }

    
}
