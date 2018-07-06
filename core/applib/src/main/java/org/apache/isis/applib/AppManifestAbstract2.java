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
package org.apache.isis.applib;

import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.fixturescripts.FixtureScript;

/**
 * Convenience adapter, configured using a {@link AppManifestAbstract.Builder}.
 */
public abstract class AppManifestAbstract2 extends AppManifestAbstract implements AppManifest2 {

    private final Module module;
    public AppManifestAbstract2(final AppManifestAbstract2.Builder builder) {
        super(builder);
        this.module = builder.getModule();
    }

    @Override
    @Programmatic
    public Module getModule() {
        return module;
    }

    @Override
    @Programmatic
    public FixtureScript getRefDataSetupFixture() {
        return new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                List<Module> modules = Module.Util.transitiveDependenciesOf(module);
                for (Module module : modules) {
                    FixtureScript fixtureScript = module.getRefDataSetupFixture();
                    if(fixtureScript != null) {
                        executionContext.executeChild(this, fixtureScript);
                    }
                }
            }
        };
    }

    @Override
    @Programmatic
    public FixtureScript getTeardownFixture() {
        return new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                List<Module> modules = Module.Util.transitiveDependenciesOf(module);
                Collections.reverse(modules);
                for (Module module : modules) {
                    final FixtureScript fixtureScript = module.getTeardownFixture();
                    if(fixtureScript != null) {
                        executionContext.executeChild(this, fixtureScript);
                    }
                }
            }
        };
    }


    /**
     * Default implementation of {@link AppManifestAbstract2} that is built using a {@link AppManifestAbstract2.Builder}.
     */
    public static class Default extends AppManifestAbstract2 {
        public Default(final AppManifestAbstract2.Builder builder) {
            super(builder);
        }
    }

    public static class Builder extends AppManifestAbstract.BuilderAbstract<Builder> {

        /**
         * Factory method.
         */
        public static AppManifestAbstract2.Builder forModule(Module module) {
            return new Builder(module);
        }

        private final Module module;

        private Builder(Module module) {
            this.module = module;
            withTransitiveFrom(module);
        }

        public Module getModule() {
            return module;
        }

        @Override
        public AppManifest2 build() {
            return new Default(this);
        }
    }

}
