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

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.core.specsupport.scenarios.ScenarioExecution;
import org.apache.isis.core.specsupport.specs.CukeStepDefsAbstract;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

/**
 * Base class for integration tests.
 * 
 * <p>
 * There is substantial overlap with {@link CukeStepDefsAbstract}, and it would be
 * possible to combine.  However, the code is quite simple; chosen not to in order
 * to make it easier to see the equivalence of these two classes.
 * 
 */
public abstract class IntegrationTestAbstract {

    protected static ScenarioExecution scenarioExecution;

    // //////////////////////////////////////

    
    /**
     * Convenience method
     */
    public void put(String type, String id, Object value) {
        scenarioExecution().put(type, id, value);
    }
    
    /**
     * Convenience method
     */
    public Object get(String type, String id) {
        return scenarioExecution().get(type, id);
    }

    /**
     * Convenience method
     */
    public <X> X get(String type, String id, Class<X> cls) {
        return scenarioExecution().get(type, id ,cls);
    }

    /**
     * Convenience method
     */
    protected <T> T service(Class<T> cls) {
        return scenarioExecution().service(cls);
    }
    
    /**
     * Convenience method
     */
    protected DomainObjectContainer container() {
        return scenarioExecution().container();
    }
    
    /**
     * Convenience method
     */
    protected WrapperFactory wrapperFactory() {
        return scenarioExecution().wrapperFactory();
    }

    /**
     * Convenience method
     */
    protected <T> T wrap(T obj) {
        return scenarioExecution().wrapperFactory().wrap(obj);
    }

    /**
     * Convenience method
     */
    protected <T> T unwrap(T obj) {
        return scenarioExecution().wrapperFactory().unwrap(obj);
    }


    private ScenarioExecution scenarioExecution() {
        return scenarioExecution;
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
                    isft.beginTran();
                    try {
                        base.evaluate();
                        isft.commitTran();
                    } catch(Throwable e) {
                        isft.bounceSystem();
                        throw e;
                    }
                }
            };
        }
    }

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Rule
    public ExpectedException expectedExceptions = ExpectedException.none();
}

