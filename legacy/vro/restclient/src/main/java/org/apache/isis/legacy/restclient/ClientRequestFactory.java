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
package org.apache.isis.legacy.restclient;

import java.net.URI;

import org.apache.isis.viewer.restfulobjects.applib.client.ClientExecutor;

/**
 * Compatibility layer, legacy of deprecated resteasy client API.
 *
 */
public interface ClientRequestFactory {

    <T> T createProxy(Class<T> clazz);

    URI getBase();

    static ClientRequestFactory of(final ClientExecutor clientExecutor, final URI baseUri) {

        return new ClientRequestFactory() {

            @Override
            public <T> T createProxy(Class<T> clazz) {
                return RestEasyLegacy.proxy(clientExecutor.webTarget(baseUri), clazz);
            }

            @Override
            public URI getBase() {
                return baseUri;
            }

        };
    }

}
