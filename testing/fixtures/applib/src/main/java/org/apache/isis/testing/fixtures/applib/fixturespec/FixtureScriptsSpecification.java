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
package org.apache.isis.testing.fixtures.applib.fixturespec;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.testing.fixtures.applib.api.FixtureScriptWithExecutionStrategy;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureResultList;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

/**
 * Specifies the behaviour of the
 * {@link org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts#runFixtureScript(String, String) runFixtureScript}
 * menu action and the execution characteristics of (graphs of)
 * {@link org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript fixture script}s.
 *
 * @since 1.x {@index}
 */
public class FixtureScriptsSpecification {

    /**
     * Typically preferable to use the create using the {@link FixtureScriptsSpecification.Builder}
     * (obtained from {@link #builder(Class)}).
     * @param packagePrefix  - to search for fixture script implementations, eg "com.mycompany".
     * @param nonPersistedObjectsStrategy - how to handle any non-persisted objects that are added to a {@link FixtureResultList}.
     * @param multipleExecutionStrategy - whether more than one instance of the same fixture script class can be run multiple times
     * @param runScriptDefaultScriptClass - the fixture script to provide as a default in {@link FixtureScripts#runFixtureScript(String, String)} action.
     * @param recreateScriptClass - if specified, then make the {@link FixtureScripts#recreateObjectsAndReturnFirst()} action visible.
     */
    public FixtureScriptsSpecification(
            final String packagePrefix,
            final FixtureScripts.NonPersistedObjectsStrategy nonPersistedObjectsStrategy,
            final FixtureScripts.MultipleExecutionStrategy multipleExecutionStrategy,
            final Class<? extends FixtureScript> runScriptDefaultScriptClass,
            final Class<? extends FixtureScript> recreateScriptClass) {
        this.packagePrefix = packagePrefix;
        this.nonPersistedObjectsStrategy = nonPersistedObjectsStrategy;
        this.multipleExecutionStrategy = multipleExecutionStrategy;
        this.recreateScriptClass = recreateScriptClass;
        this.runScriptDefaultScriptClass = runScriptDefaultScriptClass;
    }

    private final String packagePrefix;
    private final FixtureScripts.NonPersistedObjectsStrategy nonPersistedObjectsStrategy;
    private final FixtureScripts.MultipleExecutionStrategy multipleExecutionStrategy;

    private final Class<? extends FixtureScript> recreateScriptClass;
    private final Class<? extends FixtureScript> runScriptDefaultScriptClass;

    @Programmatic
    public String getPackagePrefix() {
        return packagePrefix;
    }

    @Programmatic
    public FixtureScripts.NonPersistedObjectsStrategy getNonPersistedObjectsStrategy() {
        return nonPersistedObjectsStrategy;
    }

    /**
     * Note that this can be overridden on a fixture-by-fixture basis if the fixture implements
     * {@link FixtureScriptWithExecutionStrategy}.
     */
    @Programmatic
    public FixtureScripts.MultipleExecutionStrategy getMultipleExecutionStrategy() {
        return multipleExecutionStrategy;
    }

    @Programmatic
    public Class<? extends FixtureScript> getRunScriptDefaultScriptClass() {
        return runScriptDefaultScriptClass;
    }

    @Programmatic
    public Class<? extends FixtureScript> getRecreateScriptClass() {
        return recreateScriptClass;
    }

    public static class Builder {
        private final String packagePrefix;
        private FixtureScripts.NonPersistedObjectsStrategy nonPersistedObjectsStrategy = FixtureScripts.NonPersistedObjectsStrategy.PERSIST;
        private FixtureScripts.MultipleExecutionStrategy multipleExecutionStrategy = FixtureScripts.MultipleExecutionStrategy.EXECUTE_ONCE_BY_CLASS;
        private Class<? extends FixtureScript> recreateScriptClass = null;
        private Class<? extends FixtureScript> defaultScriptClass = null;

        public Builder(final Class<?> contextClass) {
            this(contextClass.getPackage().getName());
        }
        public Builder(final String packagePrefix) {
            this.packagePrefix = packagePrefix;
        }

        public Builder with(FixtureScripts.NonPersistedObjectsStrategy nonPersistedObjectsStrategy) {
            this.nonPersistedObjectsStrategy = nonPersistedObjectsStrategy;
            return this;
        }

        /**
         * Note that this can be overridden on a fixture-by-fixture basis if the fixture implements
         * {@link FixtureScriptWithExecutionStrategy}.
         */
        public Builder with(FixtureScripts.MultipleExecutionStrategy multipleExecutionStrategy) {
            this.multipleExecutionStrategy = multipleExecutionStrategy;
            return this;
        }
        public Builder withRecreate(Class<? extends FixtureScript> recreateScriptClass) {
            this.recreateScriptClass = recreateScriptClass;
            return this;
        }
        public Builder withRunScriptDefault(Class<? extends FixtureScript> defaultScriptClass) {
            this.defaultScriptClass = defaultScriptClass;
            return this;
        }

        public FixtureScriptsSpecification build() {
            return new FixtureScriptsSpecification(
                    packagePrefix,
                    nonPersistedObjectsStrategy, multipleExecutionStrategy,
                    defaultScriptClass, recreateScriptClass
                    );
        }
    }

    public static Builder builder(final Class<?> contextClass) {
        return new Builder(contextClass);
    }
    public static Builder builder(final String packagePrefix) {
        return new Builder(packagePrefix);
    }
}
