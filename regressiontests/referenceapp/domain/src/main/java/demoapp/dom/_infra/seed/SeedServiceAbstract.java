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
package demoapp.dom._infra.seed;

import java.util.function.Supplier;

import jakarta.inject.Inject;

import org.apache.causeway.applib.events.metamodel.MetamodelListener;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScripts;

import demoapp.dom._infra.values.ValueHolder;
import demoapp.dom._infra.values.ValueHolderFixtureFactory;
import demoapp.dom._infra.values.ValueHolderRepository;
import org.jspecify.annotations.NonNull;

public abstract class SeedServiceAbstract
implements
    SeedService,
    MetamodelListener {

    private final Supplier<FixtureScript> fixtureScriptSupplier;

    protected SeedServiceAbstract(final Supplier<FixtureScript> fixtureScriptSupplier) {
        this.fixtureScriptSupplier = fixtureScriptSupplier;
    }

    protected <T, E extends ValueHolder<T>>
    SeedServiceAbstract(final @NonNull ValueHolderRepository<T, E> entities) {
        this.fixtureScriptSupplier = ()->ValueHolderFixtureFactory.fixtureScriptFor(entities);
    }

    @Override
    public void onMetamodelLoaded() {
        fixtureScripts.run(fixtureScriptSupplier.get());
    }

    @Override
    public void seed(final FixtureScript parentFixtureScript, final FixtureScript.ExecutionContext executionContext) {
        executionContext.executeChild(parentFixtureScript, this.fixtureScriptSupplier.get());
    }

    @Inject FixtureScripts fixtureScripts;

}
