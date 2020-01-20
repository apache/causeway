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
package org.apache.isis.security.keycloak.authorization;


import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.security.authorization.standard.Authorizor;

@Service
@Named("isisSecurityKeycloak.AuthorizorKeycloak")
@Order(OrderPrecedence.EARLY)
@Qualifier("Keycloak")
public class AuthorizorKeycloak implements Authorizor {

    @Override
    public boolean isVisibleInRole(String role, Identifier identifier) {
        return isVisibleInAnyRole(identifier);
    }

    @Override
    public boolean isUsableInRole(String role, Identifier identifier) {
        return isUsableInAnyRole(identifier);
    }

    @Override
    public boolean isVisibleInAnyRole(Identifier identifier) {
        return isPermitted(identifier, "r");
    }

    @Override
    public boolean isUsableInAnyRole(Identifier identifier) {
        return isPermitted(identifier, "w");
    }

    private boolean isPermitted(Identifier identifier, String qualifier) {
        return true;
    }


}
