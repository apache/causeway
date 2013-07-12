/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.specsupport.specs;

import cucumber.api.java.Before;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.core.specsupport.scenarios.ScenarioExecution;
import org.apache.isis.core.specsupport.scenarios.ScenarioExecutionScope;

/**
 * Base class for unit-scope Cucumber step definitions.
 * 
 * <p>
 * Simply declares that an instance of {@link ScenarioExecution} (or a subclass)
 * must be instantiated by the Cucumber-JVM runtime and injected into the step definitions.
 */
public abstract class CukeStepDefsAbstract {

    private ScenarioExecution scenarioExecution;
    
    /**
     * Access the {@link ScenarioExecution} as setup through a previous call to {@link #before(ScenarioExecutionScope)}.
     * 
     * <p>
     * This corresponds, broadly, to the (Ruby) Cucumber's &quot;World&quot; object.
     */
    protected ScenarioExecution scenarioExecution() {
        if(scenarioExecution == null) {
            throw new IllegalStateException("The scenario execution has not been set up; call #before(ScenarioExecutionScope) first");
        }
        return scenarioExecution;
    }
    
    /**
     * Convenience
     */
    protected <T> T service(Class<T> cls) {
        return scenarioExecution().service(cls);
    }
    
    /**
     * Convenience
     */
    protected DomainObjectContainer container() {
        return scenarioExecution().container();
    }
    
    /**
     * Convenience
     */
    protected WrapperFactory wrapperFactory() {
        return scenarioExecution().wrapperFactory();
    }
    
    // //////////////////////////////////////

    /**
     * Indicate that a scenario is starting, and specify the {@link ScenarioExecutionScope scope} 
     * at which to run the scenario.
     * 
     * <p>
     * This method should be called from a &quot;before&quot; hook (a method annotated with
     * Cucumber's {@link Before} annotation, in a step definition subclass.  The tag
     * should be appropriate for the scope specified.  Typically this method should be delegated to 
     * twice, in two mutually exclusive before hooks.
     * 
     * <p>
     * Calling this method makes the {@link ScenarioExecution} available (via {@link #scenarioExecution()}).
     * It also delegates to the scenario to {@link ScenarioExecution#beginTran() begin the transaction}.  
     * (Whether this actually does anything depends in implementation of the {@link ScenarioExecution}). 
     * 
     * <p>
     * The boilerplate (to copy-n-paste as required) is:
     * <pre>
     *  &#64;cucumber.api.java.Before("@unit")
     *  public void beforeScenarioUnitScope() {
     *     before(ScenarioExecutionScope.UNIT);
     *  }
     *  &#64;cucumber.api.java.Before("@integration")
     *  public void beforeScenarioIntegrationScope() {
     *     before(new ScenarioExecutionScope(ScenarioExecutionForMyAppIntegration.class));
     *  }
     * </pre>
     * where <tt>ScenarioExecutionForMyAppIntegration</tt> is an application-specific subclass of
     * {@link ScenarioExecution} for integration-testing.  Typically this is done using the 
     * <tt>IsisSystemForTest</tt> class provided in the <tt>isis-core-integtestsupport</tt> module).
     * 
     * <p>
     * Not every class holding step definitions should have these hooks, only those that correspond to the logical
     * beginning and end of scenario.  As such, this method may only be called once per scenario execution
     * (and fails fast if called more than once).
     */
    protected void before(ScenarioExecutionScope scope) {
        if(scenarioExecution != null) {
            throw new IllegalStateException("Scenario execution scope has already been set");
        }
        scenarioExecution = scope.instantiate();
        scenarioExecution.beginTran();
    }

    /**
     * Indicate that a scenario is ending; the {@link ScenarioExecution} is discarded and no
     * longer {@link #scenarioExecution() available}.
     * 
     * <p>
     * Before being discarded, the {@link ScenarioExecution} is delegated to
     * in order to {@link ScenarioExecution#endTran(boolean) end the transaction}.
     * (Whether this actually does anything depends in implementation of the {@link ScenarioExecution}).
     *  
     * <p>
     * The boilerplate (to copy-n-paste as required) is:
     * <pre>
     *  &#64;cucumber.api.java.After
     *  public void afterScenario(cucumber.api.Scenario sc) {
     *     after(sc);
     *  }
     * </pre>
     * 
     * <p>
     * Not every class holding step definitions should have this hook, only those that correspond to the logical
     * beginning and end of scenario.  As such, this method may only be called once per scenario execution
     * (and fails fast if called more than once).
     */
    public void after(cucumber.api.Scenario sc) {
        if(scenarioExecution == null) {
            throw new IllegalStateException("Scenario execution is not set");
        }
        scenarioExecution.endTran(!sc.isFailed());
        scenarioExecution = null;
    }

}
