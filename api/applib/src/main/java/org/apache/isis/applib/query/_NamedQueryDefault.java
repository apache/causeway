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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

final class _NamedQueryDefault<T> 
extends _QueryAbstract<T>
implements NamedQuery<T> {

    private static final long serialVersionUID = 1L;

    @Getter(onMethod_ = {@Override})
    private final @NonNull String name;
    
    @Getter(onMethod_ = {@Override})
    private final @NonNull Map<String, Object> parametersByName;

    protected _NamedQueryDefault(
            final @NonNull Class<T> resultType, 
            final @NonNull String queryName, 
            final long start,
            final long count,
            final @Nullable Map<String, Object> parametersByName) {
        super(resultType, start, count);
        this.name = queryName;
        this.parametersByName = parametersByName==null 
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(parametersByName);
    }

    @Override
    public String getDescription() {
        return getName() + " with " + getParametersByName();
    }
    
    // -- WITHERS
    
    @Override
    public _NamedQueryDefault<T> withStart(final long start) {
        if(start<0) {
            throw _Exceptions.illegalArgument("require start>=0, got %d", start);
        }
        return new _NamedQueryDefault<>(getResultType(), getName(), start, getCount(), getParametersByName());
    }

    @Override
    public _NamedQueryDefault<T> withCount(final long count) {
        if(count<0) {
            throw _Exceptions.illegalArgument("require count>=0, got %d", count);
        }
        return new _NamedQueryDefault<>(getResultType(), getName(), getStart(), count, getParametersByName());
    }

    @Override
    public NamedQuery<T> withParameter(
            final @NonNull String parameterName, 
            final @Nullable Object parameterValue) {
        if(parameterName.isEmpty()) {
            throw _Exceptions.illegalArgument("require parameterName to be non empty, got '%s'", parameterName);
        }
        val params = parametersByName==null 
                ? new HashMap<String, Object>()
                : new HashMap<String, Object>(getParametersByName());
        params.put(parameterName, parameterValue);
        return new _NamedQueryDefault<>(getResultType(), getName(), getStart(), getCount(), params);
    }

}
