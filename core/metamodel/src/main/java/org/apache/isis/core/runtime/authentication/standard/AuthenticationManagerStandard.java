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

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.authentication.RegistrationDetails;

public class AuthenticationManagerStandard implements AuthenticationManager, DebuggableWithTitle {

    private final Map<String, String> userByValidationCode = Maps.newHashMap();

    /**
     * Not final because may be set {@link #setAuthenticators(List)
     * programmatically}.
     */
    private List<Authenticator> authenticators = Lists.newArrayList();

    private RandomCodeGenerator randomCodeGenerator;
    private final IsisConfiguration configuration;

    // //////////////////////////////////////////////////////////
    // constructor
    // //////////////////////////////////////////////////////////

    public AuthenticationManagerStandard(final IsisConfiguration configuration) {
        this.configuration = configuration;
    }

    // //////////////////////////////////////////////////////////
    // init
    // //////////////////////////////////////////////////////////

    /**
     * Will default the {@link #setRandomCodeGenerator(RandomCodeGenerator)
     * RandomCodeGenerator}, but {@link Authenticator}(s) must have been
     * {@link #addAuthenticator(Authenticator) added} or
     * {@link #setAuthenticators(List) injected}.
     */
    @Override
    public final void init() {
        defaultRandomCodeGeneratorIfNecessary();
        addDefaultAuthenticators();
        if (authenticators.size() == 0) {
            throw new IsisException("No authenticators specified");
        }
        for (final Authenticator authenticator : authenticators) {
            authenticator.init();
        }
    }

    private void defaultRandomCodeGeneratorIfNecessary() {
        if (randomCodeGenerator == null) {
            randomCodeGenerator = new RandomCodeGenerator10Chars();
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

        final Collection<Authenticator> compatibleAuthenticators = Collections2.filter(authenticators, AuthenticatorFuncs.compatibleWith(request));
        if (compatibleAuthenticators.size() == 0) {
            throw new NoAuthenticatorException("No authenticator available for processing " + request.getClass().getName());
        }
        for (final Authenticator authenticator : compatibleAuthenticators) {
            final AuthenticationSession authSession = authenticator.authenticate(request, getUnusedRandomCode());
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

    @Override
    public final boolean isSessionValid(final AuthenticationSession session) {
        final String userName = userByValidationCode.get(session.getValidationCode());
        return session.hasUserNameOf(userName);
    }

    @Override
    public void closeSession(final AuthenticationSession session) {
        userByValidationCode.remove(session.getValidationCode());
    }

    // //////////////////////////////////////////////////////////
    // Authenticators
    // //////////////////////////////////////////////////////////

    /**
     * Adds an {@link Authenticator}.
     * 
     * <p>
     * Use either this or alternatively {@link #setAuthenticators(List) inject}
     * the full list of {@link Authenticator}s.
     */
    public final void addAuthenticator(final Authenticator authenticator) {
        authenticators.add(authenticator);
    }

    /**
     * Adds an {@link Authenticator} to the start of the list (not API).
     */
    protected void addAuthenticatorToStart(final Authenticator authenticator) {
        authenticators.add(0, authenticator);
    }

    /**
     * Provide direct injection.
     * 
     * <p>
     * Use either this or programmatically
     * {@link #addAuthenticator(Authenticator)}.
     */
    public void setAuthenticators(final List<Authenticator> authenticators) {
        this.authenticators = authenticators;
    }

    public List<Authenticator> getAuthenticators() {
        return Collections.unmodifiableList(authenticators);
    }

    // //////////////////////////////////////////////////////////
    // register
    // //////////////////////////////////////////////////////////

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
        final List<Registrar> registrars = Lists.transform(authenticators2, Registrar.AS_REGISTRAR_ELSE_NULL);
        return Lists.newArrayList(Collections2.filter(registrars, Registrar.NON_NULL));
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
     * For injection; will {@link #defaultRandomCodeGeneratorIfNecessary()
     * default} otherwise.
     */
    public void setRandomCodeGenerator(final RandomCodeGenerator randomCodeGenerator) {
        ensureThatArg(randomCodeGenerator, is(notNullValue()), "randomCodeGenerator cannot be null");
        this.randomCodeGenerator = randomCodeGenerator;
    }

    // //////////////////////////////////////////////////////////
    // Debugging
    // //////////////////////////////////////////////////////////

    @Override
    public String debugTitle() {
        return "Authentication Manager";
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        debug.appendTitle("Authenticators");
        for (final Authenticator authenticator : authenticators) {
            debug.appendln(authenticator.toString());
        }

        debug.appendTitle("Users");
        for (final String userName : userByValidationCode.values()) {
            debug.appendln(userName);
        }
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
