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
package org.apache.isis.core.unittestsupport.soap;

import java.util.function.Supplier;

public class SoapEndpointSpec {

    static SoapEndpointSpec asSoapEndpointSpec(final Class<?> input) {
        return new SoapEndpointSpec(input);
    }

    public SoapEndpointSpec(final Class<?> endpointClass) {
        this(endpointClass, new SupplierUsingDefaultConstructor(endpointClass), null);
    }

    public SoapEndpointSpec(final Class<?> endpointClass, final String endpointAddress) {
        this(endpointClass, new SupplierUsingDefaultConstructor(endpointClass), endpointAddress);
    }

    public SoapEndpointSpec(final Class<?> endpointClass, final Supplier<?> endpointImplementorFactory) {
        this(endpointClass, endpointImplementorFactory, null);
    }

    public SoapEndpointSpec(final Class<?> endpointClass, final Supplier<?> endpointImplementorFactory, final String endpointAddress) {
        this.endpointClass = endpointClass;
        this.endpointAddress = endpointAddress;
        this.endpointImplementorFactory = endpointImplementorFactory;
    }

    private final Class<?> endpointClass;
    Class<?> getEndpointClass() {
        return endpointClass;
    }

    private final Supplier<?> endpointImplementorFactory;
    Supplier<?> getEndpointImplementorFactory() {
        return endpointImplementorFactory;
    }

    private String endpointAddress;
    String getEndpointAddress() {
        return endpointAddress;
    }
    /**
     * Populated when published if not otherwise.
     */
    void setEndpointAddress(String endpointAddress) {
        this.endpointAddress = endpointAddress;
    }

    private static class SupplierUsingDefaultConstructor implements Supplier<Object> {
        private final Class<?> endpointClass;

        public SupplierUsingDefaultConstructor(final Class<?> endpointClass) {
            this.endpointClass = endpointClass;
        }

        @Override
        public Object get() {
            try {
                return endpointClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
