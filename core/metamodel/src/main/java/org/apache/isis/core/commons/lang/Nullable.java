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
package org.apache.isis.core.commons.lang;

public class Nullable<T> {

    public static <T> Nullable<T> some(T t) {
        return new Nullable<>(t);
    }

    public static <T> Nullable<T> none() {
        return new Nullable<T>(null);
    }

    private final T t;

    private Nullable(final T t) {
        this.t = t;
    }

    public boolean isPresent() { return t != null; }
    public T value() {
        return t;
    }

}
