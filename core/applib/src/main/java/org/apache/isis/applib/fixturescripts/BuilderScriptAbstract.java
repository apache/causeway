/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
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
package org.apache.isis.applib.fixturescripts;

import org.apache.isis.applib.annotation.Programmatic;

public abstract class BuilderScriptAbstract<T extends BuilderScriptAbstract>
        extends FixtureScript implements FixtureScriptWithExecutionStrategy {

    private final FixtureScripts.MultipleExecutionStrategy executionStrategy;

    /**
     * Typically we expect builders to have value semantics, so this is provided as a convenience.
     */
    protected BuilderScriptAbstract() {
        this(FixtureScripts.MultipleExecutionStrategy.EXECUTE_ONCE_BY_VALUE);
    }

    protected BuilderScriptAbstract(final FixtureScripts.MultipleExecutionStrategy executionStrategy) {
        this.executionStrategy = executionStrategy;
    }

    @Override
    public FixtureScripts.MultipleExecutionStrategy getMultipleExecutionStrategy() {
        return executionStrategy;
    }

    @Programmatic
    public T build(
            final FixtureScript parentFixtureScript,
            ExecutionContext executionContext) {

        // returns the fixture script that is run
        // (either this one, or possibly one previously executed).
        return (T)executionContext.executeChildT(parentFixtureScript, this);
    }

    @Programmatic
    public T build(ExecutionContext executionContext) {

        final FixtureScript anonymousParent = new FixtureScript() {
            @Override
            protected void execute(ExecutionContext executionContext) { }
        };

        return build(anonymousParent, executionContext);
    }


}

