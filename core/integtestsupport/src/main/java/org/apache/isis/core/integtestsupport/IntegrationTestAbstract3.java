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
package org.apache.isis.core.integtestsupport;

import java.util.List;

import com.google.common.base.Throwables;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.Module;
import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.services.jdosupport.IsisJdoSupport;
import org.apache.isis.core.runtime.headless.logging.LogConfig;
import org.apache.isis.core.runtime.headless.HeadlessWithBootstrappingAbstract;
import org.apache.isis.core.runtime.headless.IsisSystem;

/**
 * Reworked base class for integration tests, uses a {@link Module} to bootstrap, rather than an {@link AppManifest}.
 */
public abstract class IntegrationTestAbstract3 extends HeadlessWithBootstrappingAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTestAbstract3.class);

    @Rule
    public ExpectedException expectedExceptions = ExpectedException.none();

    /**
     * this is asymmetric - handles only the teardown of the transaction afterwards, not the initial set up
     * (which is done instead by the @Before, so that can also bootstrap system the very first time)
     */
    @Rule
    public IntegrationTestAbstract3.IsisTransactionRule isisTransactionRule = new IntegrationTestAbstract3.IsisTransactionRule();

    private static class IsisTransactionRule implements MethodRule {

        @Override
        public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {

            return new Statement() {
                @Override
                public void evaluate() throws Throwable {

                    // we don't set up the ISFT, because the very first time it won't be there.
                    // Instead we expect it to be bootstrapped via @Before
                    try {
                        base.evaluate();
                        final IsisSystem isft = IsisSystem.get();
                        isft.endTran();
                    } catch(final Throwable e) {
                        // determine if underlying cause is an applib-defined exception,
                        final RecoverableException recoverableException =
                                determineIfRecoverableException(e);
                        final NonRecoverableException nonRecoverableException =
                                determineIfNonRecoverableException(e);

                        if(recoverableException != null) {
                            try {
                                final IsisSystem isft = IsisSystem.get();
                                isft.getContainer().flush(); // don't care if npe
                                isft.getService(IsisJdoSupport.class).getJdoPersistenceManager().flush();
                            } catch (Exception ignore) {
                                // ignore
                            }
                        }
                        // attempt to close this
                        try {
                            final IsisSystem isft = IsisSystem.getElseNull();
                            isft.closeSession(); // don't care if npe
                        } catch(Exception ignore) {
                            // ignore
                        }

                        // attempt to start another
                        try {
                            final IsisSystem isft = IsisSystem.getElseNull();
                            isft.openSession(); // don't care if npe
                        } catch(Exception ignore) {
                            // ignore
                        }


                        // if underlying cause is an applib-defined, then
                        // throw that rather than Isis' wrapper exception
                        if(recoverableException != null) {
                            throw recoverableException;
                        }
                        if(nonRecoverableException != null) {
                            throw nonRecoverableException;
                        }

                        // report on the error that caused
                        // a problem for *this* test
                        throw e;
                    }
                }

                NonRecoverableException determineIfNonRecoverableException(final Throwable e) {
                    NonRecoverableException nonRecoverableException = null;
                    final List<Throwable> causalChain2 = Throwables.getCausalChain(e);
                    for (final Throwable cause : causalChain2) {
                        if(cause instanceof NonRecoverableException) {
                            nonRecoverableException = (NonRecoverableException) cause;
                            break;
                        }
                    }
                    return nonRecoverableException;
                }

                RecoverableException determineIfRecoverableException(final Throwable e) {
                    RecoverableException recoverableException = null;
                    final List<Throwable> causalChain = Throwables.getCausalChain(e);
                    for (final Throwable cause : causalChain) {
                        if(cause instanceof RecoverableException) {
                            recoverableException = (RecoverableException) cause;
                            break;
                        }
                    }
                    return recoverableException;
                }
            };
        }
    }


    protected IntegrationTestAbstract3(final Module module) {
        this(new LogConfig(Level.INFO), module);
    }

    protected IntegrationTestAbstract3(
            final LogConfig logConfig,
            final Module module) {
        super(logConfig, module);
    }

    @Override
    @Before
    public void bootstrapAndSetupIfRequired() {

        super.bootstrapAndSetupIfRequired();

        log("### TEST: " + this.getClass().getCanonicalName());
    }

    @Override
    @After
    public void tearDownAllModules() {
        super.tearDownAllModules();
    }

}