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

package org.apache.isis.core.security.authentication.manager;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.security.authentication.Authentication;
import org.apache.isis.core.security.authentication.standard.NoAuthenticatorException;
import org.apache.isis.core.security.authentication.standard.RandomCodeGenerator;
import org.apache.isis.core.security.authentication.standard.SimpleAuthentication;
import org.apache.isis.core.security.authentication.AuthenticationRequest;
import org.apache.isis.core.security.authentication.standard.Authenticator;
import org.apache.isis.core.security.authentication.standard.Registrar;

import lombok.Getter;
import lombok.val;

@Service
@Named("isisSecurityApi.AuthenticationManager")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class AuthenticationManager {

    private final Map<String, String> userByValidationCode = _Maps.newHashMap();

    private final RandomCodeGenerator randomCodeGenerator;
    @Getter
    private final List<Authenticator> authenticators;
    private final List<Registrar> registrars;

    @Inject
    public AuthenticationManager(
            final List<Authenticator> authenticators,
            final RandomCodeGenerator randomCodeGenerator) {
        this.randomCodeGenerator = randomCodeGenerator;
        this.authenticators = authenticators;
        if (authenticators.isEmpty()) {
            throw new NoAuthenticatorException("No authenticators specified");
        }

        registrars = authenticators.stream()
                .filter(Registrar.class::isInstance)
                .map(Registrar.class::cast)
                .collect(_Lists.toUnmodifiable());
    }

    // -- SESSION MANAGEMENT (including authenticate)

    public synchronized final Authentication authenticate(AuthenticationRequest request) {
        
        if (request == null) {
            return null;
        }

        val compatibleAuthenticators = _NullSafe.stream(authenticators)
                .filter(authenticator->authenticator.canAuthenticate(request.getClass()))
                .collect(Collectors.toList());
                
        if (compatibleAuthenticators.size() == 0) {
            throw new NoAuthenticatorException("No authenticator available for processing " + request.getClass().getName());
        }
        
        for (final Authenticator authenticator : compatibleAuthenticators) {
            val authSession = authenticator.authenticate(request, getUnusedRandomCode());
            if (authSession != null) {
                userByValidationCode.put(authSession.getValidationCode(), authSession.getUserName());
                return authSession;
            }
        }
        
        return null;
    }
    
    private String getUnusedRandomCode() {
        String code;
        do {
            code = randomCodeGenerator.generateRandomCode();
        } while (userByValidationCode.containsKey(code));

        return code;
    }


    public final boolean isSessionValid(final Authentication session) {
        if(session==null) {
            return false;
        }
        if(session instanceof SimpleAuthentication) {
            final SimpleAuthentication simpleSession = (SimpleAuthentication) session;
            if(simpleSession.getType() == Authentication.Type.EXTERNAL) {
                return true;
            }
        }
        final String userName = userByValidationCode.get(session.getValidationCode());
        return session.getUser().isCurrentUser(userName);
    }


    public void closeSession(Authentication session) {
        for (Authenticator authenticator : authenticators) {
            authenticator.logout(session);
        }
        userByValidationCode.remove(session.getValidationCode());
    }

    // -- AUTHENTICATORS

    public boolean register(RegistrationDetails registrationDetails) {
        for (val registrar : this.registrars) {
            if (registrar.canRegister(registrationDetails.getClass())) {
                return registrar.register(registrationDetails);
            }
        }
        return false;
    }


    public boolean supportsRegistration(Class<? extends RegistrationDetails> registrationDetailsClass) {
        for (val registrar : this.registrars) {
            if (registrar.canRegister(registrationDetailsClass)) {
                return true;
            }
        }
        return false;
    }



    // -- DEBUGGING
 
    private static final ToString<AuthenticationManager> toString =
            ToString.<AuthenticationManager>toString("class", obj->obj.getClass().getSimpleName())
            .thenToString("authenticators", obj->""+obj.authenticators.size())
            .thenToString("users", obj->""+obj.userByValidationCode.size());

    @Override
    public String toString() {
        return toString.toString(this);
    }



}
