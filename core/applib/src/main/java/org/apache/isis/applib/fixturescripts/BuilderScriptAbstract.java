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
package org.apache.isis.applib.fixturescripts;

import java.util.List;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.commons.internal.collections._Lists;

@Programmatic
public abstract class BuilderScriptAbstract<T,F extends BuilderScriptAbstract<T,F>>
extends FixtureScript implements WithPrereqs<T,F>, FixtureScriptWithExecutionStrategy {

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
    public F build(
            final FixtureScript parentFixtureScript,
            ExecutionContext executionContext) {

        final F onFixture = self();
        parentFixtureScript.serviceRegistry.injectServicesInto(onFixture);

        execPrereqs(executionContext);

        // returns the fixture script that is run
        // (either this one, or possibly one previously executed).
        return executionContext.executeChildT(parentFixtureScript, this).self();
    }

    @Override
    public void execPrereqs(final ExecutionContext executionContext) {
        final F onFixture = self();
        for (final WithPrereqs.Block<T,F> prereq : prereqs) {
            prereq.execute(onFixture, executionContext);
        }
    }

    @Override
    protected abstract void execute(final ExecutionContext executionContext);

    public abstract T getObject();

    public <P extends PersonaWithBuilderScript<T, F>> T objectFor(
            final P persona,
            final FixtureScript.ExecutionContext ec) {
        if(persona == null) {
            return null;
        }
        final F fixtureScript = persona.builder();
        return ec.executeChildT(this, fixtureScript).getObject();
    }

    public <P extends PersonaWithFinder<T>> T findUsing(final P persona) {
        if(persona == null) {
            return null;
        }
        return persona.findUsing(serviceRegistry);
    }

    private final List<WithPrereqs.Block<T,F>> prereqs = _Lists.newArrayList();

    @Override
    public F setPrereq(WithPrereqs.Block<T,F> prereq) {
        prereqs.add(prereq);
        return self();
    }

    @SuppressWarnings("unchecked") //[ahuber] it's safe to assume that this object is of type F
    protected F self() {
        return (F)this;
    }

}

