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
import org.apache.isis.applib.services.wrapper.WrapperFactory;
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
 */
public abstract class IntegrationTestAbstract {

    protected static ScenarioExecution scenarioExecution() {
        return ScenarioExecution.current();
    }

    // //////////////////////////////////////

  
    /**
     * Intended to be called whenever there is a logically distinct interaction
     * with the system.
     *
     * <p>
     *     Each transaction has its own instances of request-scoped services, most notably
     *     the {@link org.apache.isis.applib.services.command.Command}.
     * </p>
     * 
     * <p>
     *     (Unlike {@link #nextSession()}), it <i>is</i> valid to hold references to objects across transactions.
     * </p>
     *
     * @see #nextRequest()
     * @see #nextSession()
     */
    protected void nextTransaction() {
        scenarioExecution().endTran(true);
        scenarioExecution().beginTran();
    }

    /**
     * Synonym for {@link #nextTransaction()}.
     *
     * @see #nextTransaction()
     * @see #nextSession()
     */
    protected void nextRequest() {
        nextTransaction();
    }

    /**
     * Completes the transaction and session, then opens another session and transaction.
     *
     * <p>
     *     Note that any references to objects must be discarded and reacquired.
     * </p>
     *
     * @see #nextTransaction()
     * @see #nextRequest()
     */
    protected void nextSession() {
        scenarioExecution().endTran(true);
        scenarioExecution().closeSession();
        scenarioExecution().openSession();
        scenarioExecution().beginTran();
    }

    protected FixtureClock getFixtureClock() {
        return ((FixtureClock)FixtureClock.getInstance());
    }


    // //////////////////////////////////////

    
    /**
     * Convenience method
     */
    public Object getVar(final String type, final String id) {
        return scenarioExecution().getVar(type, id);
    }

    /**
     * Convenience method
     */
    public <X> X getVar(final String type, final String id, final Class<X> cls) {
        return scenarioExecution().getVar(type, id ,cls);
    }

    /**
     * Convenience method
     */
    public void putVar(final String type, final String id, final Object value) {
        scenarioExecution().putVar(type, id, value);
    }
    
    /**
     * Convenience method
     */
    public void removeVar(final String type, final String id) {
        scenarioExecution().removeVar(type, id);
    }

    /**
     * Convenience method
     *
     * @deprecated - instead just inject service into test.
     */
    @Deprecated
    protected <T> T service(final Class<T> cls) {
        return scenarioExecution().service(cls);
    }
    
    /**
     * Convenience method
     *
     * @deprecated - instead just inject {@link org.apache.isis.applib.DomainObjectContainer} into test.
     */
    @Deprecated
    protected DomainObjectContainer container() {
        return scenarioExecution().container();
    }
    
    /**
     * Convenience method
     *
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
                    } catch(final Throwable e) {
                        isft.bounceSystem();
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

