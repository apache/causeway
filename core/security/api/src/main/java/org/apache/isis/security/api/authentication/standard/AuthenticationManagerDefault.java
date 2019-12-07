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

package org.apache.isis.security.api.authentication.standard;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.security.api.authentication.AuthenticationRequest;
import org.apache.isis.security.api.authentication.AuthenticationSession;
import org.apache.isis.security.api.authentication.manager.AuthenticationManager;
import org.apache.isis.security.api.authentication.manager.RegistrationDetails;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import static org.apache.isis.commons.internal.base._With.requires;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import lombok.val;

@Service
@Named("isisSecurityApi.AuthenticationManagerDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@Log4j2
public class AuthenticationManagerDefault implements AuthenticationManager {

    @Inject private ServiceRegistry serviceRegistry;

    private final Map<String, String> userByValidationCode = _Maps.newHashMap();
    private final _Lazy<RandomCodeGenerator> randomCodeGenerator =
            _Lazy.threadSafe(this::getDefaultRandomCodeGenerator);
    
    @Getter private RandomCodeGenerator defaultRandomCodeGenerator = new RandomCodeGenerator10Chars();
    @Getter private Can<Authenticator> authenticators;

    @PostConstruct
    public void init() {
        authenticators = serviceRegistry.select(Authenticator.class);
        if (authenticators.isEmpty()) {
            throw new NoAuthenticatorException("No authenticators specified");
        }
    }

    // -- SESSION MANAGEMENT (including authenticate)

    @Override
    public synchronized final AuthenticationSession authenticate(AuthenticationRequest request) {
        
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
            code = randomCodeGenerator.get().generateRandomCode();
        } while (userByValidationCode.containsKey(code));

        return code;
    }


    @Override
    public final boolean isSessionValid(final AuthenticationSession session) {
        if(session instanceof SimpleSession) {
            final SimpleSession simpleSession = (SimpleSession) session;
            if(simpleSession.getType() == AuthenticationSession.Type.EXTERNAL) {
                return true;
            }
        }
        final String userName = userByValidationCode.get(session.getValidationCode());
        return session.hasUserNameOf(userName);
    }


    @Override
    public void closeSession(AuthenticationSession session) {
        for (Authenticator authenticator : authenticators) {
            authenticator.logout(session);
        }
        userByValidationCode.remove(session.getValidationCode());
    }

    // -- AUTHENTICATORS

    @Override
    public boolean register(RegistrationDetails registrationDetails) {
        for (val registrar : getRegistrars()) {
            if (registrar.canRegister(registrationDetails.getClass())) {
                return registrar.register(registrationDetails);
            }
        }
        return false;
    }


    @Override
    public boolean supportsRegistration(Class<? extends RegistrationDetails> registrationDetailsClass) {
        for (val registrar : getRegistrars()) {
            if (registrar.canRegister(registrationDetailsClass)) {
                return true;
            }
        }
        return false;
    }

    private final _Lazy<List<Registrar>> registrars = _Lazy.threadSafe(this::toRegistrars);
    
    public List<Registrar> getRegistrars() {
        return registrars.get();
    }

    private List<Registrar> toRegistrars() {
        return getAuthenticators().stream()
                .map(Registrar.AS_REGISTRAR_ELSE_NULL)
                .filter(_NullSafe::isPresent)
                .collect(_Lists.toUnmodifiable());
    }

    // -- RANDOM CODE GENERATOR
 
    public void setRandomCodeGenerator(RandomCodeGenerator randomCodeGenerator) {
        requires(randomCodeGenerator, "randomCodeGenerator");
        this.defaultRandomCodeGenerator = randomCodeGenerator;
        this.randomCodeGenerator.clear(); // invalidate
    }

    // -- DEBUGGING
 
    private final static ToString<AuthenticationManagerDefault> toString =
            ToString.<AuthenticationManagerDefault>toString("class", obj->obj.getClass().getSimpleName())
            .thenToString("authenticators", obj->""+obj.authenticators.size())
            .thenToString("users", obj->""+obj.userByValidationCode.size());

    @Override
    public String toString() {
        return toString.toString(this);
    }


    /**
     * JUnit Test Support
     * @param mockAuthenticator
     * @return
     */
    public static AuthenticationManagerDefault instanceForTesting(Authenticator authenticator) {
        val manager = new AuthenticationManagerDefault();
        manager.authenticators = Can.ofSingleton(authenticator);
        return manager;
    }


}
