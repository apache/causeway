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

import lombok.NonNull;

final class _AllInstancesQueryDefault<T> 
extends QueryAbstract<T> 
implements AllInstancesQuery<T> {

    private static final long serialVersionUID = 1L;

    protected _AllInstancesQueryDefault(
            final @NonNull Class<T> type, 
            final long start, 
            final long count) {
        super(type, start, count);
    }

    @Deprecated
    protected _AllInstancesQueryDefault(final String typeName, final long start, final long count) {
        super(typeName, start, count);
    }

    @Override
    public String getDescription() {
        return getResultTypeName() + " (all instances)";
    }

    // -- WITHERS
    
    @Override
    public _AllInstancesQueryDefault<T> withStart(final long start) {
        if(start<0) {
            throw _Exceptions.illegalArgument("require start>=0, got %d", start);
        }
        return new _AllInstancesQueryDefault<>(getResultType(), start, getCount());
    }

    @Override
    public _AllInstancesQueryDefault<T> withCount(final long count) {
        if(count<0) {
            throw _Exceptions.illegalArgument("require count>=0, got %d", count);
        }
        return new _AllInstancesQueryDefault<>(getResultType(), getStart(), count);
    }

}
