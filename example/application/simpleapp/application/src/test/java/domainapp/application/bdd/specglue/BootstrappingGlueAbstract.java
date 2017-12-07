/**
O *  Licensed to the Apache Software Foundation (ASF) under one or more
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
package domainapp.application.bdd.specglue;

import org.apache.isis.applib.Module;
import org.apache.isis.core.integtestsupport.IntegrationTestAbstract3;
import org.apache.isis.core.specsupport.scenarios.ScenarioExecutionScope;
import org.apache.isis.core.specsupport.specs.CukeGlueAbstract;

import cucumber.api.java.After;
import cucumber.api.java.Before;

public abstract class BootstrappingGlueAbstract extends CukeGlueAbstract {

    private final Module module;
    private final Class[] additionalModuleClasses;

    public BootstrappingGlueAbstract(final Module module, final Class... additionalModuleClasses) {
        this.module = module;
        this.additionalModuleClasses = additionalModuleClasses;
    }

    @Before(value={"@integration"}, order=100)
    public void beforeScenarioIntegrationScope() {

        IntegrationTestAbstract3 integTest =
                new IntegrationTestAbstract3(module, additionalModuleClasses) {};
        integTest.bootstrapAndSetupIfRequired();

        before(ScenarioExecutionScope.INTEGRATION);

        scenarioExecution().putVar(IntegrationTestAbstract3.class.getName(), "current", integTest);
    }

    @After
    public void afterScenario(cucumber.api.Scenario sc) {
        assertMocksSatisfied();

        IntegrationTestAbstract3 integTest =
                scenarioExecution().getVar(IntegrationTestAbstract3.class.getName(), "current",
                        IntegrationTestAbstract3.class);
        integTest.tearDownAllModules();

        after(sc);
    }
}
