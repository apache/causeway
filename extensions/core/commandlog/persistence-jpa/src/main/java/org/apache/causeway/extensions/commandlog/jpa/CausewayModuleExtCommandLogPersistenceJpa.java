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
package org.apache.causeway.extensions.commandlog.jpa;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.jpa.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.jpa.dom.CommandLogEntryPK;
import org.apache.causeway.extensions.commandlog.jpa.dom.CommandLogEntryRepository;
import org.apache.causeway.persistence.jpa.eclipselink.CausewayModulePersistenceJpaEclipselink;
import org.apache.causeway.testing.fixtures.applib.CausewayModuleTestingFixturesApplib;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.causeway.testing.fixtures.applib.modules.ModuleWithFixtures;
import org.apache.causeway.testing.fixtures.applib.teardown.jpa.TeardownFixtureJpaAbstract;

/**
 * @since 2.0 {@index}
 */
@Configuration
@Import({
        // modules
        CausewayModuleTestingFixturesApplib.class,
        CausewayModuleExtCommandLogApplib.class,
        CausewayModulePersistenceJpaEclipselink.class,

        // @Service's
        CommandLogEntryRepository.class,
        CommandLogEntryPK.Semantics.class,

        // entities
        CommandLogEntry.class
})
@EntityScan(basePackageClasses = {
    CommandLogEntry.class
})
public class CausewayModuleExtCommandLogPersistenceJpa {

    public static final String NAMESPACE = CausewayModuleExtCommandLogApplib.NAMESPACE;
    public static final String SCHEMA = CausewayModuleExtCommandLogApplib.SCHEMA;

    /**
     * Note that this is <i>NOT</i> an implementation of the {@link ModuleWithFixtures#getTeardownFixture()} API;
     * but is provided to allow manual teardown if required.
     */
    public FixtureScript teardownFixture() {
        return new TeardownFixtureJpaAbstract() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                deleteFrom(CommandLogEntry.class);
            }
        };
    }

}
