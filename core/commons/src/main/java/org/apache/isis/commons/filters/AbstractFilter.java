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


package org.apache.isis.commons.filters;



public abstract class AbstractFilter<T> implements Filter<T> {

    public abstract boolean accept(T f);

    public Filter<T> and(final Filter<T> f) {
        return Filters.and(this, f);
    }

    public Filter<T> or(final Filter<T> f) {
        return Filters.or(this, f);
    }

    public Filter<T> not() {
        return Filters.not(this);
    }

    public final static <T> Filter<T> noop(final Class<T> clazz) {
        return new AbstractFilter<T>() {
            @Override
            public boolean accept(final T f) {
                return true;
            }
        };
    }

}
