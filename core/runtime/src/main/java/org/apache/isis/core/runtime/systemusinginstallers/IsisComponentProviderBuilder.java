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
package org.apache.isis.core.runtime.systemusinginstallers;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.core.runtime.authorization.standard.AuthorizationManagerStandard;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authentication.standard.AuthenticationManagerStandard;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;

import static org.apache.isis.commons.internal.base._With.computeIfAbsent;

public class IsisComponentProviderBuilder {
    
    private AppManifest appManifest;
    private AuthenticationManager authenticationManager;
    private AuthorizationManager authorizationManager;
    
    public IsisComponentProviderBuilder appManifest(AppManifest appManifest) {
        this.appManifest = appManifest;
        return this;
    }
    
    public IsisComponentProviderBuilder authenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        return this;
    }
    
    public IsisComponentProviderBuilder authorizationManager(AuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
        return this;
    }
    
    // -- BUILD
    
    public IsisComponentProvider build() {
        
        authenticationManager = computeIfAbsent(authenticationManager, 
                IsisComponentProviderBuilder::authenticationManagerWithBypass);
        
        authorizationManager = computeIfAbsent(authorizationManager, 
                AuthorizationManagerStandard::new);
        
        return new IsisComponentProvider(appManifest, authenticationManager, authorizationManager);
    }
    
    // -- HELPER
    
    
    /**
     * The standard authentication manager, configured with the 'bypass' authenticator 
     * (allows all requests through).
     * <p>
     * integration tests ignore appManifest for authentication and authorization.
     */
    private static AuthenticationManager authenticationManagerWithBypass() {
        final AuthenticationManagerStandard authenticationManager = new AuthenticationManagerStandard();
        authenticationManager.addAuthenticator(new AuthenticatorBypass());
        return authenticationManager;
    }
    
    
}
