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
package org.apache.causeway.extensions.executionlog.jpa;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.extensions.executionlog.applib.CausewayModuleExtExecutionLogApplib;
import org.apache.causeway.extensions.executionlog.jpa.dom.ExecutionLogEntry;
import org.apache.causeway.extensions.executionlog.jpa.dom.ExecutionLogEntryPK;
import org.apache.causeway.extensions.executionlog.jpa.dom.ExecutionLogEntryRepository;
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
        CausewayModuleExtExecutionLogApplib.class,
        CausewayModulePersistenceJpaEclipselink.class,

        // @Service's
        ExecutionLogEntryRepository.class,
        ExecutionLogEntryPK.Semantics.class,

        // entities
        ExecutionLogEntry.class
})
@EntityScan(basePackageClasses = {
        ExecutionLogEntry.class
})
public class CausewayModuleExtExecutionLogPersistenceJpa implements ModuleWithFixtures {

    public static final String NAMESPACE = CausewayModuleExtExecutionLogApplib.NAMESPACE;
    public static final String SCHEMA = CausewayModuleExtExecutionLogApplib.SCHEMA;

    @Override
    public FixtureScript getTeardownFixture() {
        return new TeardownFixtureJpaAbstract() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                deleteFrom(ExecutionLogEntry.class);
            }
        };
    }

}
