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

package org.apache.isis.core.runtime.headless;

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.fixtures.FixtureClock;
import org.apache.isis.commons.internal.base._Blackhole;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.config.AppConfigLocator;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.runtime.headless.auth.AuthenticationRequestNameOnly;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.session.IsisSessionProducerBean;
import org.apache.isis.core.security.authentication.AuthenticationRequest;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;

import lombok.val;


/**
 * Wraps a plain {@link IsisSessionFactoryBuilder}.
 *
 * <p>
 *     This is a simplification of <tt>IsisSystemForTest</tt>, removing dependencies on junit and specsupport.
 * </p>
 */
public final class IsisSystem {

    // -- getElseNull, get, set

    protected static ThreadLocal<IsisSystem> ISFT = new ThreadLocal<>();

    public static IsisSystem getElseNull() {
        return ISFT.get();
    }

    public static IsisSystem get() {
        final IsisSystem isft = ISFT.get();
        if(isft == null) {
            throw new IllegalStateException("No IsisSystem available on thread; call #set(IsisSystem) first");
        }

        return isft;
    }

    public static void set(IsisSystem isft) {
        ISFT.set(isft);
    }


    public static IsisSystem ofConfiguration(IsisConfiguration isisConfiguration) {

        AppManifest appManifest = isisConfiguration.getAppManifest();
        AuthenticationRequest authenticationRequest = new AuthenticationRequestNameOnly("tester");

        return new IsisSystem(appManifest, authenticationRequest);
    }

    // -- constructor, fields

    protected final AppManifest appManifest;
    protected final AuthenticationRequest authenticationRequestIfAny;
    protected AuthenticationSession authenticationSession;


    private IsisSystem(
            final AppManifest appManifest,
            final AuthenticationRequest authenticationRequestIfAny) {
        this.appManifest = appManifest;
        this.authenticationRequestIfAny = authenticationRequestIfAny;
    }

    // -- setup (also componentProvider)

    IsisSystem setUpSystem() throws RuntimeException {
        try {
            initIfRequiredThenOpenSession();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    protected void initIfRequiredThenOpenSession() throws Exception {

        // exit as quickly as possible for this case...
        {
            val metaModelDeficiencies = IsisContext.getMetaModelDeficienciesIfAny();

            if(metaModelDeficiencies != null) {
                final Set<String> validationErrors = metaModelDeficiencies.getValidationErrors();
                final String validationMsg = _NullSafe.stream(validationErrors)
                        .collect(Collectors.joining("\n")); 
                throw new AssertionError(validationMsg);
            }
        }

        boolean firstTime = isisSessionFactory == null;
        if(firstTime) {

            // ensures that a FixtureClock is installed as the singleton underpinning the ClockService
            FixtureClock.initialize();

            // finalize the config (build and regard immutable)
            // as a side-effect bootstrap CDI, if the environment we are running on does not already have its own 
            _Blackhole.consume(AppConfigLocator.getAppConfig());

            isisSessionFactory = new IsisSessionProducerBean().produceIsisSessionFactory();
            
            // REVIEW: does no harm, but is this required?
            closeSession();

            // if the IsisSystem does not initialize properly, then - as a side effect - the resulting
            // MetaModelInvalidException will be pushed onto the IsisContext (as a static field).
            val metaModelDeficiencies = IsisContext.getMetaModelDeficienciesIfAny();

            if (metaModelDeficiencies != null) {

                // for subsequent tests; the attempt to bootstrap the framework will leave
                // the IsisContext singleton as set.
                IsisContext.clear();

                final Set<String> validationErrors = metaModelDeficiencies.getValidationErrors();
                final StringBuilder buf = new StringBuilder();
                for (String validationError : validationErrors) {
                    buf.append(validationError).append("\n");
                }
                throw new AssertionError("Metamodel is invalid: \n" + buf.toString());
            }
        }

        final AuthenticationManager authenticationManager = isisSessionFactory.getAuthenticationManager();
        authenticationSession = authenticationManager.authenticate(authenticationRequestIfAny);

        openSession();
    }

    // -- isisSystem (populated during setup)
    protected IsisSessionFactory isisSessionFactory;

    /**
     * The {@link IsisSessionFactory} created during {@link #setUpSystem()}.
     */
    public IsisSessionFactory getIsisSessionFactory() {
        return isisSessionFactory;
    }

    /**
     * The {@link AuthenticationSession} created during {@link #setUpSystem()}.
     */
    public AuthenticationSession getAuthenticationSession() {
        return authenticationSession;
    }

    // -- openSession, closeSession, nextSession


    public void nextSession() throws Exception {
        closeSession();
        openSession();
    }

    public void openSession() throws Exception {
        openSession(authenticationSession);
    }

    public void openSession(AuthenticationSession authenticationSession) throws Exception {
        isisSessionFactory.openSession(authenticationSession);
    }

    public void closeSession() throws Exception {
        if(isisSessionFactory!=null && isisSessionFactory.isInSession()) {
            isisSessionFactory.closeSession();
        }
    }

    // -- getService

    public <C> C getService(Class<C> serviceClass) {
        return IsisContext.getServiceRegistry().lookupServiceElseFail(serviceClass);
    }


}
