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
package org.apache.causeway.viewer.restfulobjects.client.auth;

import org.apache.causeway.viewer.restfulobjects.client.RestfulClientConfig;
import org.apache.causeway.viewer.restfulobjects.client.auth.basic.AuthorizationHeaderFactoryBasic;
import org.apache.causeway.viewer.restfulobjects.client.auth.oauth2.azure.AuthorizationHeaderFactoryOauth2Azure;

public interface AuthorizationHeaderFactory {

    static AuthorizationHeaderFactory factoryFor(
            final RestfulClientConfig restfulClientConfig) {
        var authenticationMode = restfulClientConfig.getAuthenticationMode();
        if (authenticationMode == null) {
            throw new IllegalArgumentException("config.authenticationMode must be set");
        }

        switch (authenticationMode) {
            case BASIC:
                return new AuthorizationHeaderFactoryBasic(restfulClientConfig);
            case OAUTH2_AZURE:
                return new AuthorizationHeaderFactoryOauth2Azure(restfulClientConfig);
            default:
                throw new IllegalArgumentException(String.format("unknown authenticationMode '%s'", authenticationMode));
        }
    }

    String create();
}
