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

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.fixtures.FixtureClock;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.scratchpad.Scratchpad;
import org.apache.isis.applib.services.sessmgmt.SessionManagementService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.specsupport.scenarios.ScenarioExecution;
import org.apache.isis.core.specsupport.specs.CukeGlueAbstract;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

/**
 * Base class for integration tests.
 * 
 * <p>
 * There is substantial overlap with {@link CukeGlueAbstract}, and it would be possible to factor
 * out a common base class.  Both delegate to an underlying {@link ScenarioExecution}, and provide
 * a bunch of helper methods.  The reason this has not been done is mostly to make it easier to see 
 * the equivalence of these two classes.
 * 
 * <p>
 * The only real differences between this class and {@link CukeGlueAbstract} is that this class 
 * uses JUnit rules to automatically perform {@link IsisTransactionRule transaction management} and
 * uses JUnit rules for {@link ExpectedException exception handling}.  In {@link CukeGlueAbstract} these
 * are required (by Cucumber-JVM) to be explicitly handled in the step definitions.
 *
 * @deprecated - to be replaced by {@link IntegrationTestAbstract3}
 */
@Deprecated
public abstract class IntegrationTestAbstract {

    /**
     * @deprecated - just inject domain services into test instead.
     */
    @Deprecated
    protected static ScenarioExecution scenarioExecution() {
        return ScenarioExecution.current();
    }

    // //////////////////////////////////////

  
    /**
     * @deprecated - instead just inject {@link TransactionService} into test and use {@link TransactionService#nextTransaction()} instead.
     */
    @Deprecated
    protected void nextTransaction() {
        scenarioExecution().endTran(true);
        scenarioExecution().beginTran();
    }

    /**
     * @deprecated - instead just inject {@link SessionManagementService} or {@link TransactionService} into test and use either {@link SessionManagementService#nextSession()} or {@link TransactionService#nextTransaction()} instead.
     */
    @Deprecated
    protected void nextRequest() {
        nextTransaction();
    }

    /**
     * @deprecated - instead just inject {@link SessionManagementService} into test and use {@link SessionManagementService#nextSession()} instead.
     */
    @Deprecated
    protected void nextSession() {
        scenarioExecution().endTran(true);
        scenarioExecution().closeSession();
        scenarioExecution().openSession();
        scenarioExecution().beginTran();
    }

    /**
     * If just require the current time, use {@link ClockService}.
     */
    protected FixtureClock getFixtureClock() {
        return ((FixtureClock)FixtureClock.getInstance());
    }


    // //////////////////////////////////////

    
    /**
     * @deprecated - just inject {@link Scratchpad} service into test and use {@link Scratchpad#get(Object)} instead.
     */
    @Deprecated
    public Object getVar(final String type, final String id) {
        return scenarioExecution().getVar(type, id);
    }

    /**
     * @deprecated - just inject {@link Scratchpad} service into test and use {@link Scratchpad#get(Object)} instead.
     */
    @Deprecated
    public <X> X getVar(final String type, final String id, final Class<X> cls) {
        return scenarioExecution().getVar(type, id ,cls);
    }

    /**
     * @deprecated - just inject {@link Scratchpad} service into test and use {@link Scratchpad#put(Object, Object)} instead.
     */
    @Deprecated
    public void putVar(final String type, final String id, final Object value) {
        scenarioExecution().putVar(type, id, value);
    }

    /**
     * @deprecated - just inject {@link Scratchpad} service into test and use {@link Scratchpad#put(Object, Object)} (setting to <tt>null</tt>) instead.
     */
    @Deprecated
    public void removeVar(final String type, final String id) {
        scenarioExecution().removeVar(type, id);
    }

    /**
     * @deprecated - instead just inject service into test; optionally use {@link ServiceRegistry} service to lookup other services.
     */
    @Deprecated
    protected <T> T service(final Class<T> cls) {
        return scenarioExecution().service(cls);
    }
    
    /**
     * @deprecated - instead just inject {@link org.apache.isis.applib.DomainObjectContainer} into test.
     */
    @Deprecated
    protected DomainObjectContainer container() {
        return scenarioExecution().container();
    }
    
    /**
     * @deprecated - instead just inject {@link org.apache.isis.applib.services.wrapper.WrapperFactory} into test.
     */
    @Deprecated
    protected WrapperFactory wrapperFactory() {
        return scenarioExecution().wrapperFactory();
    }

    /**
     * Convenience method
     */
    protected <T> T wrap(final T obj) {
        return scenarioExecution().wrapperFactory().wrap(obj);
    }

    /**
     * Convenience method
     */
    protected <T> T unwrap(final T obj) {
        return scenarioExecution().wrapperFactory().unwrap(obj);
    }

    /**
     * Convenience method
     */
    protected <T> T mixin(final Class<T> mixinClass, final Object mixedIn) {
        return container().mixin(mixinClass, mixedIn);
    }

    
    // //////////////////////////////////////

    /**
     * The order is important; this rule is outermost, and must - at a minimum - come before
     * the {@link #expectedExceptions} rule.
     */
    @Rule
    public IsisTransactionRule isisTransactionRule = new IsisTransactionRule();

    private static class IsisTransactionRule implements MethodRule  {

        @Override
        public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
            final IsisSystemForTest isft = IsisSystemForTest.get(); 
            
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    isft.getContainer().injectServicesInto(target);
                    isft.beginTran();
                    try {
                        base.evaluate();
                        isft.endTran();
                        isft.nextSession();
                    } catch(final Throwable e) {

                        // if test failed to clean up after itself, then take care of it here.
                        endTransactionTilDone();

                        isft.nextSession();
                        final List<Throwable> causalChain = Throwables.getCausalChain(e);
                        // if underlying cause is an applib-defined exception, throw that rather than Isis' wrapper exception
                        for (final Throwable cause : causalChain) {
                            if(cause instanceof RecoverableException ||
                               cause instanceof NonRecoverableException) {
                                throw cause;
                            }
                        }
                        throw e;
                    }
                }

                protected void endTransactionTilDone() {
                    IsisTransactionManager tranMgr = isft.getIsisSessionFactory().getCurrentSession()
                                                         .getPersistenceSession().getTransactionManager();
                    int count = 0;
                    while(tranMgr.getTransactionLevel() > 0 &&
                          count++ < 10              // just in case, to prevent an infinite loop...
                            ) {
                        try {
                            tranMgr.endTransaction();
                        } catch(Exception ignore) {
                            // ignore
                        }
                    }
                }
            };
        }
    }

    // //////////////////////////////////////

    /**
     * Convenience method to avoid some boilerplate and rename (as more in keeping with the
     * {@link org.apache.isis.applib.fixturescripts.FixtureScript} API compared to the older
     * {@link org.apache.isis.applib.fixtures.InstallableFixture} API).
     *
     * @deprecated  - just inject {@link org.apache.isis.applib.fixturescripts.FixtureScripts} service for your application, and call.  If multiple fixture scripts, create an anonymous subclass of {@link org.apache.isis.applib.fixturescripts.FixtureScript} and override {@link org.apache.isis.applib.fixturescripts.FixtureScript#execute(org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext)} execute.
     */
    @Deprecated
    protected static void runScript(final FixtureScript... fixtureScripts) {
        scenarioExecution().install(fixtureScripts);
    }

    // //////////////////////////////////////


    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Rule
    public ExpectedException expectedExceptions = ExpectedException.none();

    @Rule
    public ExceptionRecognizerTranslate exceptionRecognizerTranslations = ExceptionRecognizerTranslate.create();
}

