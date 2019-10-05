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

package org.apache.isis.security.authentication.standard;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.security.authentication.AuthenticationRequest;
import org.apache.isis.security.authentication.AuthenticationSession;
import org.apache.isis.security.authentication.manager.AuthenticationManager;
import org.apache.isis.security.authentication.manager.RegistrationDetails;

import static org.apache.isis.commons.internal.base._NullSafe.stream;
import static org.apache.isis.commons.internal.base._With.requires;

import lombok.Getter;
import lombok.val;

public class AuthenticationManagerStandard implements AuthenticationManager {

    private final Map<String, String> userByValidationCode = _Maps.newHashMap();
    private final List<Authenticator> authenticators = _Lists.newArrayList();
    private _Lazy<RandomCodeGenerator> randomCodeGenerator =
            _Lazy.threadSafe(this::getDefaultRandomCodeGenerator);
    
    @Getter
    private RandomCodeGenerator defaultRandomCodeGenerator = new RandomCodeGenerator10Chars();

    @Inject
    private ServiceRegistry serviceRegistry;

    @PostConstruct
    public void preInit() {
        serviceRegistry.select(Authenticator.class).forEach(authenticators::add);
    }


    // //////////////////////////////////////////////////////////
    // init
    // //////////////////////////////////////////////////////////

    /**
     * Will default the {@link #setRandomCodeGenerator(RandomCodeGenerator)
     * RandomCodeGenerator}, but {@link Authenticator}(s) must have been
     * {@link #addAuthenticator(Authenticator) added}.
     * @param deploymentCategory
     */
    @Override
    public final void init() {
        addDefaultAuthenticators();
        if (authenticators.size() == 0) {
            throw new IsisException("No authenticators specified");
        }
        for (final Authenticator authenticator : authenticators) {
            authenticator.init();
        }
    }


    /**
     * optional hook method
     */
    protected void addDefaultAuthenticators() {
    }

    @Override
    public void shutdown() {
        for (final Authenticator authenticator : authenticators) {
            authenticator.shutdown();
        }
    }

    // //////////////////////////////////////////////////////////
    // Session Management (including authenticate)
    // //////////////////////////////////////////////////////////


    @Override
    public synchronized final AuthenticationSession authenticate(final AuthenticationRequest request) {
        
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
        final String userName = userByValidationCode.get(session.getValidationCode());
        return session.hasUserNameOf(userName);
    }


    @Override
    public void closeSession(final AuthenticationSession session) {
        List<Authenticator> authenticators = getAuthenticators();
        for (Authenticator authenticator : authenticators) {
            authenticator.logout(session);
        }
        userByValidationCode.remove(session.getValidationCode());
    }

    // //////////////////////////////////////////////////////////
    // Authenticators
    // //////////////////////////////////////////////////////////


    public final void addAuthenticator(final Authenticator authenticator) {
        authenticators.add(authenticator);
    }


    public void addAuthenticatorToStart(final Authenticator authenticator) {
        authenticators.add(0, authenticator);
    }


    public List<Authenticator> getAuthenticators() {
        return Collections.unmodifiableList(authenticators);
    }



    @Override
    public boolean register(final RegistrationDetails registrationDetails) {
        for (final Registrar registrar : getRegistrars()) {
            if (registrar.canRegister(registrationDetails.getClass())) {
                return registrar.register(registrationDetails);
            }
        }
        return false;
    }


    @Override
    public boolean supportsRegistration(final Class<? extends RegistrationDetails> registrationDetailsClass) {
        for (final Registrar registrar : getRegistrars()) {
            if (registrar.canRegister(registrationDetailsClass)) {
                return true;
            }
        }
        return false;
    }


    public List<Registrar> getRegistrars() {
        return asAuthenticators(getAuthenticators());
    }

    private static List<Registrar> asAuthenticators(final List<Authenticator> authenticators2) {
        return stream(authenticators2)
                .map(Registrar.AS_REGISTRAR_ELSE_NULL)
                .filter(Registrar.NON_NULL)
                .collect(Collectors.toList());
    }

    // //////////////////////////////////////////////////////////
    // RandomCodeGenerator
    // //////////////////////////////////////////////////////////

    public void setRandomCodeGenerator(final RandomCodeGenerator randomCodeGenerator) {
        requires(randomCodeGenerator, "randomCodeGenerator");
        this.defaultRandomCodeGenerator = randomCodeGenerator;
        this.randomCodeGenerator.clear(); // invalidate
    }

    // //////////////////////////////////////////////////////////
    // Debugging
    // //////////////////////////////////////////////////////////

    private final static ToString<AuthenticationManagerStandard> toString =
            ToString.<AuthenticationManagerStandard>toString("class", obj->obj.getClass().getSimpleName())
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
    public static AuthenticationManagerStandard getInstance(Authenticator authenticator) {
        val manager = new AuthenticationManagerStandard();
        manager.authenticators.add(authenticator);
        return manager;
    }

}
