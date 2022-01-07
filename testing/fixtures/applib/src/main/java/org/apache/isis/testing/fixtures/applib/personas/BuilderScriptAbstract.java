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
package org.apache.isis.testing.fixtures.applib.personas;

import org.apache.isis.applib.annotations.Programmatic;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScriptWithExecutionStrategy;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

import lombok.Getter;

/**
 * A specialization of {@link FixtureScript} that is intended to be used to
 * setup the state of a {@link PersonaWithBuilderScript persona}.
 *
 * <p>
 *     The {@link PersonaWithBuilderScript persona} represents the &quot;what&quot;, in other words the raw data,
 *     while the {@link BuilderScriptAbstract build script} represents the &quot;how-to&quot;.
 * </p>
 * @since 2.x {@index}
 */
public abstract class BuilderScriptAbstract<T>
extends FixtureScript implements FixtureScriptWithExecutionStrategy {

    @Getter(onMethod=@__({@Override}))
    private final FixtureScripts.MultipleExecutionStrategy multipleExecutionStrategy;

    // -- FACTORIES

    /**
     * Typically we expect builders to have value semantics, so this is provided as a convenience.
     */
    protected BuilderScriptAbstract() {
        this(FixtureScripts.MultipleExecutionStrategy.EXECUTE_ONCE_BY_VALUE);
    }

    protected BuilderScriptAbstract(final FixtureScripts.MultipleExecutionStrategy executionStrategy) {
        this.multipleExecutionStrategy = executionStrategy;
    }

    // -- RESULTING OBJECT

    public abstract T getObject();

    // -- RUN ANOTHER SCRIPT

    @Programmatic
    public BuilderScriptAbstract<T> build(
            final FixtureScript parentFixtureScript,
            final ExecutionContext executionContext) {

        parentFixtureScript.getServiceInjector().injectServicesInto(this);

        // returns the fixture script that is run
        // (either this one, or possibly one previously executed).
        return executionContext.executeChildT(parentFixtureScript, this);
    }

    // -- RUN PERSONAS

    public T objectFor(
            final PersonaWithBuilderScript<BuilderScriptAbstract<T>> persona,
            final ExecutionContext executionContext) {

        if(persona == null) {
            return null;
        }
        final BuilderScriptAbstract<T> fixtureScript = persona.builder();
        return executionContext.executeChildT(this, fixtureScript).getObject();
    }

    // -- RUN FINDERS

    public T findUsing(final PersonaWithFinder<T> persona) {
        if(persona == null) {
            return null;
        }
        return persona.findUsing(serviceRegistry);
    }

}

