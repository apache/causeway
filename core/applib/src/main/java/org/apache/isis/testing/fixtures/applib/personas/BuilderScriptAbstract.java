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

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScriptWithExecutionStrategy;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

@Programmatic
public abstract class BuilderScriptAbstract<T, B extends BuilderScriptAbstract<T, B>>
        extends FixtureScript
        implements WithPrereqs<T, B>, FixtureScriptWithExecutionStrategy {

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
    public B build(
            final FixtureScript parentFixtureScript,
            ExecutionContext executionContext) {

        final B onFixture = (B) BuilderScriptAbstract.this;
        parentFixtureScript.getContainer().injectServicesInto(onFixture);

        execPrereqs(executionContext);

        // returns the fixture script that is run
        // (either this one, or possibly one previously executed).
        return (B)executionContext.executeChildT(parentFixtureScript, this);
    }

    @Override
    public void execPrereqs(final ExecutionContext executionContext) {
        final B onFixture = (B) BuilderScriptAbstract.this;
        for (final WithPrereqs.Block<T, B> prereq : prereqs) {
            prereq.execute(onFixture, executionContext);
        }
    }

    @Override
    protected abstract void execute(final ExecutionContext executionContext);

    public abstract T getObject();

    public <P extends Persona<X, B>, X, B extends BuilderScriptAbstract<X, B>> X objectFor(
            final P persona,
            final FixtureScript.ExecutionContext ec) {
        if(persona == null) {
            return null;
        }
        final B fixtureScript = persona.builder();
        return ec.executeChildT(this, fixtureScript).getObject();
    }

    public <P extends PersonaWithFinder<X>, X> X findUsing(final P persona) {
        if(persona == null) {
            return null;
        }
        return persona.findUsing(serviceRegistry);
    }

    private final List<WithPrereqs.Block<T, B>> prereqs = Lists.newArrayList();

    @Override
    public B setPrereq(WithPrereqs.Block<T, B> prereq) {
        prereqs.add(prereq);
        return (B)this;
    }


}

