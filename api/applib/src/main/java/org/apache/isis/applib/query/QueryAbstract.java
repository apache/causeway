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

import org.apache.isis.core.commons.internal.context._Context;

import lombok.Getter;

/**
 * Convenience adapter class for {@link Query}.
 *
 * <p>
 * Handles implementation of {@link #getResultType()}
 */
public abstract class QueryAbstract<T> implements Query<T> {

    private static final long serialVersionUID = 1L;

    /**
     * The start index within the retrieved result set.
     */
    @Getter
    protected long start;

    /**
     * The number of items to return
     */
    @Getter
    protected long count;

    @Getter
    private final String resultTypeName;

    /**
     * Derived from {@link #getResultTypeName()}, with respect to the
     * {@link Thread#getContextClassLoader() current thread's class loader}.
     */
    private transient Class<T> resultType;

    /**
     * Base query based on Class type.
     *
     * @param type
     */
    public QueryAbstract(final Class<T> type, final long start, final long count) {
        this.resultTypeName = type.getName();
        this.start = start;
        this.count = count;
    }

    public QueryAbstract(final String typeName, final long start, final long count) {
        this.resultTypeName = typeName;
        this.start = start;
        this.count = count;
    }

    /**
     * @throws IllegalStateException
     *             (wrapping a {@link ClassNotFoundException}) if the class
     *             could not be determined.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getResultType() {
        if (resultType == null) {
            try {
                resultType = (Class<T>) _Context.loadClass(resultTypeName);
            } catch (final ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        return resultType;
    }

}
