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


package org.apache.isis.core.runtime.authentication.standard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.authentication.NoAuthenticatorException;
import org.apache.isis.core.runtime.authentication.standard.exploration.ExplorationAuthenticator;
import org.apache.isis.core.runtime.authentication.standard.fixture.LogonFixtureAuthenticator;
import org.apache.isis.core.runtime.context.IsisContext;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;


public class AuthenticationManagerStandard implements AuthenticationManager, DebuggableWithTitle {

    private List<Authenticator> authenticators = new ArrayList<Authenticator>();
    private final Map<String, String> userByValidationCode = new HashMap<String, String>();

    private RandomCodeGenerator randomCodeGenerator;
    private IsisConfiguration configuration;

    // //////////////////////////////////////////////////////////
    // constructor
    // //////////////////////////////////////////////////////////

    public AuthenticationManagerStandard(IsisConfiguration configuration) {
        this.configuration = configuration;
    }

    // //////////////////////////////////////////////////////////
    // init
    // //////////////////////////////////////////////////////////

    /**
     * Will default the {@link #setRandomCodeGenerator(RandomCodeGenerator) RandomCodeGenerator}, but
     * {@link Authenticator}(s) must have been {@link #addAuthenticator(Authenticator) added} or
     * {@link #setAuthenticators(List) injected}.
     */
    public final void init() {
        defaultRandomCodeGeneratorIfNecessary();
        addDefaultAuthenticators();
        if (authenticators.size() == 0) {
            throw new IsisException("No authenticators specified");
        }
        for (Authenticator authenticator : authenticators) {
            authenticator.init();
        }
    }

    private void defaultRandomCodeGeneratorIfNecessary() {
        if (randomCodeGenerator == null) {
            randomCodeGenerator = new RandomCodeGenerator10Chars();
        }
    }

    private void addDefaultAuthenticators() {
        // we add to start to ensure that these special case authenticators
        // are always consulted first
        addAuthenticatorToStart(new ExplorationAuthenticator(getConfiguration()));
        addAuthenticatorToStart(new LogonFixtureAuthenticator(getConfiguration()));
    }

    public void shutdown() {
        for (Authenticator authenticator : authenticators) {
            authenticator.shutdown();
        }
    }

    // //////////////////////////////////////////////////////////
    // Session Management (including authenticate)
    // //////////////////////////////////////////////////////////

    public synchronized final AuthenticationSession authenticate(final AuthenticationRequest request) {
        if (request == null) {
            return null;
        }

        for (Authenticator authenticator : authenticators) {
            if (authenticator.canAuthenticate(request)) {
                AuthenticationSession authSession = null;
                authSession = authenticator.authenticate(request, getUnusedRandomCode());
                if (authSession != null) {
                    userByValidationCode.put(authSession.getValidationCode(), authSession.getUserName());
                }
                return authSession;
            }
        }
        throw new NoAuthenticatorException("No authenticator available for processing " + request.getClass().getName());
    }

    private String getUnusedRandomCode() {
        String code;
        do {
            code = randomCodeGenerator.generateRandomCode();
        } while (userByValidationCode.containsKey(code));

        return code;
    }

    public final boolean isSessionValid(final AuthenticationSession session) {
        final String userName = userByValidationCode.get(session.getValidationCode());
        return session.hasUserNameOf(userName);
    }

    public void closeSession(final AuthenticationSession session) {
        userByValidationCode.remove(session.getValidationCode());
        IsisContext.closeSession();
    }

    // //////////////////////////////////////////////////////////
    // Authenticators
    // //////////////////////////////////////////////////////////

    /**
     * Adds an {@link Authenticator}.
     * 
     * <p>
     * Use either this or alternatively {@link #setAuthenticators(List) inject} the full list of
     * {@link Authenticator}s.
     */
    public final void addAuthenticator(final Authenticator authenticator) {
        authenticators.add(authenticator);
    }

    /**
     * Adds an {@link Authenticator} to the start of the list (not API).
     */
    private void addAuthenticatorToStart(final Authenticator authenticator) {
        authenticators.add(0, authenticator);
    }

    /**
     * Provide direct injection.
     * 
     * <p>
     * Use either this or programmatically {@link #addAuthenticator(Authenticator)}.
     */
    public void setAuthenticators(final List<Authenticator> authenticators) {
        this.authenticators = authenticators;
    }

    public List<Authenticator> getAuthenticators() {
        return Collections.unmodifiableList(authenticators);
    }

    // //////////////////////////////////////////////////////////
    // RandomCodeGenerator
    // //////////////////////////////////////////////////////////

    /**
     * The {@link RandomCodeGenerator} in use.
     */
    public RandomCodeGenerator getRandomCodeGenerator() {
        return randomCodeGenerator;
    }

    /**
     * For injection; will {@link #defaultRandomCodeGeneratorIfNecessary() default} otherwise.
     */
    public void setRandomCodeGenerator(final RandomCodeGenerator randomCodeGenerator) {
        ensureThatArg(randomCodeGenerator, is(notNullValue()), "randomCodeGenerator cannot be null");
        this.randomCodeGenerator = randomCodeGenerator;
    }

    // //////////////////////////////////////////////////////////
    // Debugging
    // //////////////////////////////////////////////////////////

    public String debugTitle() {
        return "Authentication Manager";
    }

    public void debugData(final DebugString debug) {
        debug.appendTitle("Authenticators");
        debug.indent();
        for (Authenticator authenticator : authenticators) {
            debug.appendln(authenticator.toString());
        }
        debug.unindent();

        debug.appendTitle("Users");
        debug.indent();
        for (String userName : userByValidationCode.values()) {
            debug.appendln(userName);
        }
        debug.unindent();
    }

    @Override
    public String toString() {
        final ToString str = ToString.createAnonymous(this);
        str.append("authenticators", authenticators.size());
        str.append("users", userByValidationCode.size());
        return str.toString();
    }

    // //////////////////////////////////////////////////////////
    // Injected (constructor)
    // //////////////////////////////////////////////////////////

    protected IsisConfiguration getConfiguration() {
        return configuration;
    }

}

