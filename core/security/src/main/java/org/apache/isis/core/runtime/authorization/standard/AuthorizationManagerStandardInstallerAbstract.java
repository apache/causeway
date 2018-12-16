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

package org.apache.isis.core.runtime.authorization.standard;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.commons.components.InstallerAbstract;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManagerInstaller;

public abstract class AuthorizationManagerStandardInstallerAbstract
extends InstallerAbstract
implements AuthorizationManagerInstaller {

    public AuthorizationManagerStandardInstallerAbstract(final String name) {
        super(name);
    }

    @Override
    public AuthorizationManager createAuthorizationManager() {
        
        try {
            return createAuthorizationManagerReflective();
            
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException
                | NoSuchMethodException | SecurityException | InstantiationException e) {
            
            throw new RuntimeException("unable to create AuthorizationManager reflective", e);
            
        }
    }
    
    private AuthorizationManager createAuthorizationManagerReflective() 
            throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, 
            InvocationTargetException, 
            NoSuchMethodException, SecurityException, InstantiationException {
        
        final String authorizationManagerStandardClsName = 
                "org.apache.isis.core.runtime.authorization.standard.AuthorizationManagerStandard";
        
        Class<? extends AuthorizationManager> cls = 
                (Class<? extends AuthorizationManager>) _Context
                .loadClassAndInitialize(authorizationManagerStandardClsName);
        
        AuthorizationManager authorizationManager = cls.newInstance();
        final Authorizor authorizor = createAuthorizor();
        
        cls.getMethod("setAuthorizor", new Class[] {Authorizor.class})
        .invoke(authorizationManager, new Object[] {authorizor});
        
        return authorizationManager;
    }
    
    
//    @Override
//    public AuthorizationManager createAuthorizationManager() {
//        final AuthorizationManagerStandard authorizationManager = new AuthorizationManagerStandard();
//        final Authorizor authorizor = createAuthorizor();
//        authorizationManager.setAuthorizor(authorizor);
//        
//        return authorizationManager;
//    }

    /**
     * Hook method
     */
    protected abstract Authorizor createAuthorizor();

    @Override
    public List<Class<?>> getTypes() {
        return listOf(AuthorizationManager.class);
    }

}
