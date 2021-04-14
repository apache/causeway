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

import javax.inject.Inject;

import org.springframework.context.event.EventListener;

import org.apache.isis.core.metamodel.events.MetamodelEvent;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

public abstract class SeedServiceAbstract implements SeedService {

    private final Supplier<FixtureScript> fixtureScriptSupplier;

    protected SeedServiceAbstract(Supplier<FixtureScript> fixtureScriptSupplier) {
        this.fixtureScriptSupplier = fixtureScriptSupplier;
    }

    @EventListener(MetamodelEvent.class)
    public void onAppLifecycleEvent(MetamodelEvent event) {
        if (event.isPostMetamodel()) {
            fixtureScripts.run(fixtureScriptSupplier.get());
        }
    }

    @Override
    public void seed(FixtureScript parentFixtureScript, FixtureScript.ExecutionContext executionContext) {
        executionContext.executeChild(parentFixtureScript, this.fixtureScriptSupplier.get());
    }

    @Inject
    FixtureScripts fixtureScripts;

}
