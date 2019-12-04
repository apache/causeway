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

package org.apache.isis.runtime.services.auth;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.sudo.SudoService;
import org.apache.isis.metamodel.authorization.standard.AuthorizationFacetFactory;
import org.apache.isis.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.metamodel.progmodel.ProgrammingModel.FacetProcessingOrder;
import org.apache.isis.security.api.authentication.AuthenticationSession;
import org.apache.isis.security.api.authorization.manager.AuthorizationManager;
import org.apache.isis.security.api.authorization.standard.Authorizor;

import lombok.val;

@Service
public class AuthorizationManagerStandard implements AuthorizationManager, MetaModelRefiner {

    @Inject protected Authorizor authorizor;

    // -- LIFECYCLE

    @PostConstruct
    public void init() {
        authorizor.init();
    }

    @PreDestroy
    public void shutdown() {
        if(authorizor == null) {
            return;
        }
        authorizor.shutdown();
    }

    // -- API

    @Override
    public boolean isUsable(final AuthenticationSession session, final Identifier identifier) {
        if (isPerspectiveMember(identifier)) {
            return true;
        }
        if(containsSudoSuperuserRole(session)) {
            return true;
        }
        if (authorizor.isUsableInAnyRole(identifier)) {
            return true;
        }

        if(session.streamRoles()
                .anyMatch(roleName->authorizor.isUsableInRole(roleName, identifier)) ) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isVisible(final AuthenticationSession session, final Identifier identifier) {
        if (isPerspectiveMember(identifier)) {
            return true;
        }

        // no-op if is visibility context check at object-level
        if (identifier.getMemberName().equals("")) {
            return true;
        }

        if(containsSudoSuperuserRole(session)) {
            return true;
        }
        if (authorizor.isVisibleInAnyRole(identifier)) {
            return true;
        }
        if(session.streamRoles()
                .anyMatch(roleName->authorizor.isVisibleInRole(roleName, identifier)) ) {
            return true;
        }
        return false;
    }

    private static boolean containsSudoSuperuserRole(final AuthenticationSession session) {
        return session.hasRole(SudoService.ACCESS_ALL_ROLE);
    }

    private boolean isPerspectiveMember(final Identifier identifier) {
        return (identifier.getClassName().equals(""));
    }

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {
        val authorizationFacetFactory = new AuthorizationFacetFactory();
        programmingModel.addFactory(FacetProcessingOrder.Z0_BEFORE_FINALLY, authorizationFacetFactory);
    }

}
