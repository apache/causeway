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
package org.apache.isis.testing.unittestsupport.applib.dom.pojo;

/**
 * @since 2.0 {@index}
 */
public class DataForBean {

    @SuppressWarnings("unchecked")
    public static <T> PojoTester.DatumFactory<T> beans(Class<T> compileTimeType, Class<? extends T> runtimeType) {
        try {
            final T obj1 = runtimeType.getDeclaredConstructor().newInstance();
            final T obj2 = runtimeType.getDeclaredConstructor().newInstance();
            return new PojoTester.DatumFactory<>(compileTimeType, obj1, obj2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> PojoTester.DatumFactory<T> beans(Class<T> type) {
        return beans(type, type);
    }

}
