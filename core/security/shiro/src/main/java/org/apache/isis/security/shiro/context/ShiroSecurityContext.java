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
package org.apache.isis.security.shiro.context;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;

import org.apache.isis.security.api.authentication.standard.Authenticator;
import org.apache.isis.security.api.authorization.standard.Authorizor;

/**
 * @since 2.0
 */
public final class ShiroSecurityContext {

    /**
     * The {@link SecurityManager} is shared between both the {@link Authenticator} and the {@link Authorizor}
     * (if shiro is configured for both components).
     */
    public static RealmSecurityManager getSecurityManager() {
        SecurityManager securityManager;
        try {
            securityManager = SecurityUtils.getSecurityManager();
        } catch(UnavailableSecurityManagerException ex) {
            return null;
        }
        if(!(securityManager instanceof RealmSecurityManager)) {
            return null;
        }
        return (RealmSecurityManager) securityManager;
    }

}
