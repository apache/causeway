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
package org.apache.isis.specsupport.scenarios;

import org.apache.isis.commons.internal.context._Context;

/**
 * The scope at which the specification will run; acts as a factory to create
 * an instance of the appropriate subclass of {@link ScenarioExecution}.
 *
 * @deprecated - with no replacement
 */
@Deprecated
public class ScenarioExecutionScope {

    public final static ScenarioExecutionScope UNIT = new ScenarioExecutionScope(ScenarioExecutionForUnit.class);
    public final static ScenarioExecutionScope INTEGRATION = new ScenarioExecutionScope("org.apache.isis.integtestsupport.scenarios.ScenarioExecutionForIntegration");

    private final Class<? extends ScenarioExecution> scenarioExecutionClass;

    public ScenarioExecutionScope(Class<? extends ScenarioExecution> scenarioExecutionClass) {
        this.scenarioExecutionClass = scenarioExecutionClass;
    }

    @SuppressWarnings("unchecked")
    public ScenarioExecutionScope(String scenarioExecutionClassName) {
        try {
            this.scenarioExecutionClass = (Class<? extends ScenarioExecution>)
                    _Context.loadClass(scenarioExecutionClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ScenarioExecution instantiate() {
        try {
            return scenarioExecutionClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return scenarioExecutionClass.getName();
    }
}
