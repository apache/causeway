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

/**
 * Convenience adapter class for {@link Query}.
 * 
 * <p>
 * Handles implementation of {@link #getResultType()}
 */
public abstract class QueryAbstract<T> implements Query<T> {

    protected long start;
    protected long count;
    private static final long serialVersionUID = 1L;

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
     * @param range optional start and count of the range of dataset. 0
     */
    public QueryAbstract(final Class<T> type, final long ... range) {
        this.resultTypeName = type.getName();
        this.start = range.length > 0 ? range[0]:0;
        this.count = range.length > 1 ? range[1]:0;
    }

    public QueryAbstract(final String typeName, final long ... range) {
        this.resultTypeName = typeName;
        this.start = range.length > 0 ? range[0]:0;
        this.count = range.length > 1 ? range[1]:0;
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
                resultType = (Class<T>) Thread.currentThread().getContextClassLoader().loadClass(resultTypeName);
            } catch (final ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        return resultType;
    }

    public String getResultTypeName() {
        return resultTypeName;
    }

    /**
     * The start index into the set table
     * @return
     */
    public long getStart() {
        return start;
    }


    /**
     * The number of items to return, starting at {@link QueryFindAllPaged#getStart()}
     * @return
     */
    public long getCount() {
        return count;
    }
}
