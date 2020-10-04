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
package org.apache.isis.extensions.jaxrsclient.impl;

import javax.ws.rs.core.Response;

/**
 * Created by dan on 12/02/2017.
 */
public interface JaxRsResponse {

    int getStatus();

    <T> T readEntity(final Class<T> entityType);

    class Default implements JaxRsResponse {

        private final Response response;

        public Default(final Response response) {
            this.response = response;
        }

        @Override
        public int getStatus() {
            return response.getStatus();
        }

        @Override
        public <T> T readEntity(final Class<T> entityType) {
            return response.readEntity(entityType);
        }
    }

    class ForTesting implements JaxRsResponse {

        private final int status;
        private final Object entity;

        public ForTesting(final int status, final Object entity) {
            this.status = status;
            this.entity = entity;
        }

        @Override
        public int getStatus() {
            return status;
        }

        @Override
        public <T> T readEntity(final Class<T> entityType) {
            return (T) entity;
        }
    }

}
