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

import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode @ToString
final class _QueryRangeDefault implements QueryRange {
    
    private static final long serialVersionUID = 1L;
    
    // -- FACTORY
    
    static _QueryRangeDefault of(long... range) {
        return new _QueryRangeDefault(range);
    }
    
    @Getter(onMethod_ = {@Override}) private final long start;
    @Getter(onMethod_ = {@Override}) private final long limit;

    _QueryRangeDefault(
            final long[] range) {
        this.start = range.length > 0 ? range[0] : 0L;
        this.limit = range.length > 1 ? range[1] : 0L;
        if(start<0L) {
            throw _Exceptions.illegalArgument("start cannot be a negative number, got %d", start);
        }
        if(limit<0L) {
            throw _Exceptions.illegalArgument("limit cannot be a negative number, got %d", limit);
        }
    }

    @Override
    public long getEnd() {
        // we limit to Integer.MAX_VALUE because HSQLDB blows up
        // (with a ClassCastException from Long to Integer)
        // if we return Long.MAX_VALUE
        if(!hasLimit()) {
            return (long) Integer.MAX_VALUE; 
        }
        
        final long end = getStart() + getLimit(); 
        if(end<0 // long addition overflow handling, eg. Long.MAX_VALUE + Long.MAX_VALUE = -2L
                || end > Integer.MAX_VALUE) {
            return (long) Integer.MAX_VALUE;
        }
        
        return end;
    }

    @Override
    public boolean isUnconstrained() {
        return !(hasOffset()
                    || hasLimit());
    }

    @Override
    public boolean hasOffset() {
        return getStart() != 0;
    }

    @Override
    public boolean hasLimit() {
        return getLimit() != 0;
    }
    
}