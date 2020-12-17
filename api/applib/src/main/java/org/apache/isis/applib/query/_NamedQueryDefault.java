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

/**
 * Default implementation of {@link Query} that supports parameter/argument
 * values, along with a query name.
 */
public class QueryDefault<T> extends QueryAbstract<T> {

    private static final long serialVersionUID = 1L;

    /**
     * Convenience factory method, preferable to
     * {@link #QueryDefault(Class, String, Object...) constructor} because will
     * automatically genericize.
     */
    public static <Q> QueryDefault<Q> create(final Class<Q> resultType, final String queryName, final Object... paramArgs) {
        return new QueryDefault<Q>(resultType, queryName, paramArgs);
    }

    /**
     * Convenience factory method, preferable to
     * {@link #QueryDefault(Class, String, Map) constructor} because will
     * automatically genericize.
     */
    public static <Q> QueryDefault<Q> create(final Class<Q> resultType, final String queryName, final Map<String, Object> argumentsByParameterName) {
        return new QueryDefault<Q>(resultType, queryName, argumentsByParameterName);
    }

    /**
     * Converts a list of objects [a, 1, b, 2] into a map {a -> 1; b -> 2}
     */
    private static Map<String, Object> asMap(final Object[] paramArgs) {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        boolean param = true;
        String paramStr = null;
        for (final Object paramArg : paramArgs) {
            if (param) {
                if (paramArg instanceof String) {
                    paramStr = (String) paramArg;
                } else {
                    throw new IllegalArgumentException("Parameter must be a string");
                }
            } else {
                final Object arg = paramArg;
                map.put(paramStr, arg);
                paramStr = null;
            }
            param = !param;
        }
        if (paramStr != null) {
            throw new IllegalArgumentException("Must have equal number of parameters and arguments");
        }
        return map;
    }

    private final String queryName;
    private final Map<String, Object> argumentsByParameterName;

    public QueryDefault(final Class<T> resultType, final String queryName, final Object... paramArgs) {
        this(resultType, queryName, asMap(paramArgs));
    }

    public QueryDefault(final Class<T> resultType, final String queryName, final Map<String, Object> argumentsByParameterName) {
        super(resultType, 0, 0);
        this.queryName = queryName;
        this.argumentsByParameterName = argumentsByParameterName;
    }

    public String getQueryName() {
        return queryName;
    }

    public Map<String, Object> getArgumentsByParameterName() {
        return Collections.unmodifiableMap(argumentsByParameterName);
    }

    public QueryDefault<T> withStart(final long start) {
        if(start<0) {
            throw new IllegalArgumentException("require start>=0");
        }
        this.start = start;
        return this;
    }

    public QueryDefault<T> withCount(final long count) {
        if(count<=0) {
            throw new IllegalArgumentException("require count>0");
        }
        this.count = count;
        return this;
    }

    @Override
    public String getDescription() {
        return getQueryName() + " with " + getArgumentsByParameterName();
    }

}
