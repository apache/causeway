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
package org.apache.causeway.applib.query;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.internal.exceptions._Exceptions;

record _NamedQueryDefault<T>(
        @NonNull Class<T> resultType,
        @NonNull String name,
        @NonNull QueryRange range,
        @NonNull Map<String, Object> parametersByName
        ) implements NamedQuery<T>, Serializable {

    protected _NamedQueryDefault(
            final @NonNull Class<T> resultType,
            final @NonNull String name,
            final @NonNull QueryRange range,
            final @Nullable Map<String, Object> parametersByName) {
        this.resultType = resultType;
        this.range = range;
        this.name = name;
        this.parametersByName = parametersByName==null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(parametersByName);
    }

    @Override public String getName() { return name; }
    @Override public Map<String, Object> getParametersByName() { return parametersByName; }
    @Override public Class<T> getResultType() { return resultType; }
    @Override public QueryRange getRange() { return range; }

    @Override
    public String getDescription() {
        return getName() + " with " + getParametersByName();
    }

    // -- WITHERS

    @Override
    public _NamedQueryDefault<T> withRange(final @NonNull QueryRange range) {
        return new _NamedQueryDefault<>(getResultType(),  getName(), range, getParametersByName());
    }

    @Override
    public NamedQuery<T> withParameter(
            final @NonNull String parameterName,
            final @Nullable Object parameterValue) {
        if(parameterName.isEmpty()) {
            throw _Exceptions.illegalArgument("require parameterName to be non empty, got '%s'", parameterName);
        }
        var params = parametersByName==null
                ? new HashMap<String, Object>()
                : new HashMap<String, Object>(getParametersByName());
        params.put(parameterName, parameterValue);
        return new _NamedQueryDefault<>(getResultType(), getName(), getRange(), params);
    }

}
