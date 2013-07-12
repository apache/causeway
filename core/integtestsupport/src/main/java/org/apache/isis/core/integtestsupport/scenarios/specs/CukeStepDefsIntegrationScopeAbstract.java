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
package org.apache.isis.core.integtestsupport.scenarios.specs;

import cucumber.api.java.After;
import cucumber.api.java.Before;

import org.apache.isis.core.integtestsupport.scenarios.ScenarioExecutionIntegrationScopeAbstract;
import org.apache.isis.core.unittestsupport.scenarios.specs.CukeStepDefsAbstract;


/**
 * Base class for integration-scope Cucumber step definitions.
 */
public abstract class CukeStepDefsIntegrationScopeAbstract extends CukeStepDefsAbstract<ScenarioExecutionIntegrationScopeAbstract> {

    public CukeStepDefsIntegrationScopeAbstract(ScenarioExecutionIntegrationScopeAbstract scenarioExecution) {
        super(scenarioExecution);
    }

    // //////////////////////////////////////

    /**
     * Convenience method to start transaction.
     * 
     * <p>
     * Cukes does not allow this to be annotated with {@link Before Cucumber's Before}
     * annotation.  Subclasses should therefore override, annotate, and delegate back up:
     * 
     * <pre>
     *  &#64;cucumber.api.java.Before
     *  &#64;Override
     *  public void beginTran() {
     *     super.beginTran();
     *  }
     * </pre>
     */
    public void beginTran() {
        scenarioExecution.beginTran();
    }

    /**
     * Convenience method to start transaction.
     * 
     * <p>
     * Cukes does not allow this to be annotated with {@link After Cucumber's After}
     * annotation.  Subclasses should therefore override, annotate, and delegate back up:
     * 
     * <pre>
     *  &#64;cucumber.api.java.After
     *  &#64;Override
     *  public void endTran(cucumber.api.Scenario sc) {
     *     super.endTran(sc);
     *  }
     * </pre>
     */
    public void endTran(cucumber.api.Scenario sc) {
        scenarioExecution.endTran(!sc.isFailed());
    }

}
