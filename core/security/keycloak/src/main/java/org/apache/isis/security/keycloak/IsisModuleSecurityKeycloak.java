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
package org.apache.isis.security.keycloak;

import javax.inject.Singleton;

import org.apache.isis.runtime.services.IsisModuleRuntimeServices;
import org.apache.isis.runtime.services.auth.AuthorizationManagerStandard;
import org.apache.isis.security.api.IsisModuleSecurityApi;
import org.apache.isis.security.keycloak.authentication.KeycloakAuthenticator;
import org.apache.isis.security.keycloak.authorization.KeycloakAuthorizor;
import org.apache.isis.security.keycloak.webmodule.WebModuleKeycloak;
import org.apache.isis.webapp.IsisModuleWebapp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.security.api.authentication.standard.AuthenticationManagerStandard;
import org.apache.isis.security.api.authentication.standard.Authenticator;
import org.apache.isis.security.api.authorization.standard.Authorizor;

/**
 * Configuration Bean to support Isis Security using Shiro.
 *  
 * @since 2.0
 */
@Configuration
@Import({
        // modules
        IsisModuleSecurityApi.class,
        IsisModuleRuntimeServices.class,
        IsisModuleWebapp.class,

        // @Service's
        WebModuleKeycloak.class,

})
public class IsisModuleSecurityKeycloak {

    @Bean @Singleton
    public Authenticator authenticator() {
        return new KeycloakAuthenticator();
    }

    @Bean @Singleton
    public Authorizor autorizor() {
        return new KeycloakAuthorizor();
    }


}
