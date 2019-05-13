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
package org.apache.isis.core.specsupport.specs;

import com.fasterxml.jackson.databind.Module;

import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.runtime.headless.HeadlessWithBootstrappingAbstract;
import org.apache.isis.core.runtime.headless.logging.LogConfig;

import cucumber.api.java.After;
import cucumber.api.java.Before;

/**
 * For BDD spec using headless access, there needs to be (at least) one BDD spec glue that inherits from this adapter
 * class, specifying the {@link Module} to use to bootstrap the system.
 *
 * <p>
 *     <b>This class is deprecated</b>.  It's not possible to subclass from this class, it'll result in an exception:
 *     <code>cucumber.runtime.CucumberException: You're not allowed to extend classes that define Step Definitions or
 *     hooks</code>.
 *     Instead, just inline the contents of this class.
 * </p>
 *
 * @deprecated - it's not possible to subclass from this class. Instead, just inline the contents of this class.
 */
@Deprecated
public abstract class CukeGlueBootstrappingAbstract extends HeadlessWithBootstrappingAbstract {

    protected CukeGlueBootstrappingAbstract(LogConfig logConfig, IsisConfiguration isisConfiguration) {
        super(logConfig, isisConfiguration);
    }

    @Before(order=100)
    public void beforeScenario() {
        super.bootstrapAndSetupIfRequired();
    }

    @After
    public void afterScenario(cucumber.api.Scenario sc) {
        super.tearDownAllModules();
    }

}
